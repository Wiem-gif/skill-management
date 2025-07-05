package com.example.skill_management.auth;

import com.example.skill_management.config.JwtService;
import com.example.skill_management.model.User;
import com.example.skill_management.repository.UserRepository;
import com.example.skill_management.token.Token;
import com.example.skill_management.token.TokenRepository;
import com.example.skill_management.token.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public Mono<AuthenticationResponse> register(RegisterRequest request) {
    User user = User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .build();

    return repository.save(user)
            .flatMap(savedUser -> {
              String jwtToken = jwtService.generateToken(savedUser);
              String refreshToken = jwtService.generateRefreshToken(savedUser);
              return saveUserToken(savedUser, jwtToken)
                      .thenReturn(AuthenticationResponse.builder()
                              .accessToken(jwtToken)
                              .refreshToken(refreshToken)
                              .build());
            });
  }

  public Mono<AuthenticationResponse> authenticate(AuthenticationRequest request) {
    return repository.findByEmail(request.getEmail())
            .flatMap(user -> {
              if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Mono.error(new RuntimeException("Invalid credentials"));
              }
              String jwtToken = jwtService.generateToken(user);
              String refreshToken = jwtService.generateRefreshToken(user);
              return revokeAllUserTokens(user)
                      .then(saveUserToken(user, jwtToken))
                      .thenReturn(AuthenticationResponse.builder()
                              .accessToken(jwtToken)
                              .refreshToken(refreshToken)
                              .build());
            });
  }

  private Mono<Void> saveUserToken(User user, String jwtToken) {
    Token token = Token.builder()
            .userId(user.getId())
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
    return tokenRepository.save(token).then();
  }

  private Mono<Void> revokeAllUserTokens(User user) {
    return tokenRepository.findAllValidTokenByUser(user.getId())
            .flatMap(validToken -> {
              validToken.setExpired(true);
              validToken.setRevoked(true);
              return tokenRepository.save(validToken);
            })
            .then();
  }

  public Mono<Void> refreshToken(ServerHttpRequest request, ServerHttpResponse response) {
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return Mono.empty();
    }
    String refreshToken = authHeader.substring(7);
    String userEmail = jwtService.extractUsername(refreshToken);

    return repository.findByEmail(userEmail)
            .filter(user -> jwtService.isTokenValid(refreshToken, user))
            .flatMap(user -> {
              String accessToken = jwtService.generateToken(user);
              return revokeAllUserTokens(user)
                      .then(saveUserToken(user, accessToken))
                      .then(Mono.fromRunnable(() -> {
                        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build();
                        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
                        response.writeWith(Mono.just(response.bufferFactory()
                                .wrap(authResponse.toString().getBytes())));
                      }));
            })
            .then();
  }
}

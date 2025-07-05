package com.example.skill_management.config;

import com.example.skill_management.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LogoutService implements ServerLogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        String authHeader = exchange.getExchange().getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.empty();
        }

        String jwt = authHeader.substring(7);

        return tokenRepository.findByToken(jwt)
                .flatMap(storedToken -> {
                    storedToken.setExpired(true);
                    storedToken.setRevoked(true);
                    return tokenRepository.save(storedToken);
                })
                .then();
    }
}

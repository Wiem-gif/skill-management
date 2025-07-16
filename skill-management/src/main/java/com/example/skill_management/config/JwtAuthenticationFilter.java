package com.example.skill_management.config;

import com.example.skill_management.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();


        if (path.contains("/skill-management/auth")) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String jwt = authHeaders.get(0).substring(7);
        String userEmail = jwtService.extractUsername(jwt);

        if (userEmail == null) {
            return chain.filter(exchange);
        }

        return userDetailsService.findByUsername(userEmail)
                .flatMap(userDetails ->
                        tokenRepository.findByToken(jwt)
                                .filter(token -> !token.isExpired() && !token.isRevoked())
                                .filter(token -> jwtService.isTokenValid(jwt, userDetails))
                                .flatMap(token -> {
                                    var claims = jwtService.extractAllClaims(jwt);
                                    List<String> authoritiesFromToken = claims.get("authorities", List.class);
                                    var authorities = authoritiesFromToken.stream()
                                            .map(SimpleGrantedAuthority::new)
                                            .collect(Collectors.toList());

                                    UsernamePasswordAuthenticationToken authToken =
                                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);


                                    return chain.filter(exchange)
                                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                                    Mono.just(new SecurityContextImpl(authToken))
                                            ));

                                })
                )

                .switchIfEmpty(chain.filter(exchange));
    }
}

package com.example.skill_management.filter;

import com.example.skill_management.util.JwtTokenUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

public class JwtAuthenticationFilter implements ServerSecurityContextRepository {

    private final JwtTokenUtil jwtTokenUtil;
    private final ReactiveUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                   ReactiveUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .flatMap(jwt -> {
                    String username = jwtTokenUtil.extractUsername(jwt);
                    return userDetailsService.findByUsername(username)
                            .filter(userDetails -> jwtTokenUtil.validateToken(jwt, userDetails))
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()))
                            .map(SecurityContextImpl::new);
                });
    }
}
package com.example.skill_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;

import static com.example.skill_management.Enum.Role.ADMIN;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final String[] WHITE_LIST_URL = {
            "/skill-management/auth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };

    private final JwtAuthenticationFilter jwtAuthWebFilter;
    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final ServerLogoutHandler reactiveLogoutHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(WHITE_LIST_URL).permitAll()
                        .pathMatchers("/skill-management/user/**").hasRole(ADMIN.name())
                        .anyExchange().authenticated()
                )
                .authenticationManager(reactiveAuthenticationManager)
                .addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .logout(logout -> logout
                        .logoutUrl("/skill-management/auth/logout")
                        .logoutHandler(reactiveLogoutHandler)

                        .logoutSuccessHandler((exchange, authentication) ->

                                reactor.core.publisher.Mono.empty()
                        )
                )
                .build();
    }
}

package com.example.skill_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;


import java.nio.charset.StandardCharsets;

import static com.example.skill_management.Enum.Role.*;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final String[] WHITE_LIST_URL = {
            "/skill-management/auth/**",

//            "/skill-category/**",
            "/job-titles/**",
//            "/skills/**",
            "/grades/**",
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
    private final ServerLogoutHandler reactiveLogoutHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(WHITE_LIST_URL).permitAll()
                        .pathMatchers("/skill-management/user/**").hasRole(ADMIN.name())
//                       .pathMatchers("/employee-skills/**").hasRole(ADMIN.name())
//                                .pathMatchers("/employee-skills/**").authenticated()
                                .pathMatchers(HttpMethod.GET, "/employee-skills/**")
                                .hasAnyRole("USER", "ADMIN", "QualityManager", "TechnicalManager")
                                .pathMatchers(HttpMethod.POST, "/employee-skills/**")
                                .hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/employee-skills/**")
                                .hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/employee-skills/**")
                                .hasRole("ADMIN")


                                .pathMatchers(HttpMethod.GET, "/skills/**")
                                .hasAnyRole("USER", "ADMIN", "QualityManager", "TechnicalManager")
                                .pathMatchers(HttpMethod.POST, "/skills/**")
                                .hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/skills/**")
                                .hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/skills/**")
                                .hasRole("ADMIN")

                                .pathMatchers(HttpMethod.GET, "/skill-category/**")
                                .hasAnyRole("USER", "ADMIN", "QualityManager", "TechnicalManager")
                                .pathMatchers(HttpMethod.POST, "/skill-category/**")
                                .hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/skill-category/**")
                                .hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/skill-category/**")
                                .hasRole("ADMIN")

                                .anyExchange().authenticated()
                )
                // 🔹 Gestion centralisée des erreurs 401 et 403
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                )
                .addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .logout(logout -> logout
                        .logoutUrl("/skill-management/auth/logout")
                        .logoutHandler(reactiveLogoutHandler)
                        .logoutSuccessHandler((exchange, authentication) -> reactor.core.publisher.Mono.empty())
                )
                .build();
    }
    // 🔹 Handler pour 401 Unauthorized
    private ServerAuthenticationEntryPoint unauthorizedEntryPoint() {
        return (exchange, ex) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String body = String.format(
                    "{\"code\":\"SMGT-0401\",\"message\":\"Unauthorized - Invalid or missing token\",\"status\":401}"
            );
            var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        };
    }

    // 🔹 Handler pour 403 Forbidden
    private ServerAccessDeniedHandler forbiddenHandler() {
        return (exchange, denied) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String body = String.format(
                    "{\"code\":\"SMGT-0403\",\"message\":\"Forbidden - Access Denied\",\"status\":403}"
            );
            var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        };
    }
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
    //  config.addAllowedOrigin("*");
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
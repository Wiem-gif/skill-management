package com.example.skill_management.controller;

import com.example.skill_management.dto.AuthRequest;
import com.example.skill_management.dto.AuthResponse;
import com.example.skill_management.util.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

@RestController
@RequestMapping("/skill-management/auth")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtTokenUtil jwtTokenUtil,
                          ReactiveUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        return userDetailsService.findByUsername(authRequest.email())
                .filter(userDetails -> passwordEncoder.matches(authRequest.password(), userDetails.getPassword()))
                .map(userDetails -> ResponseEntity.ok(new AuthResponse(jwtTokenUtil.generateToken(userDetails))))
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).build()));
    }
}
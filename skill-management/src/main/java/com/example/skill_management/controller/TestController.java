package com.example.skill_management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/skill-management")
public class TestController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('TECHNICAL_QUALITY_MANAGER')")
    public Mono<String> helloPFA2025() {
        return Mono.just("Hello PFA 2025");
    }
}
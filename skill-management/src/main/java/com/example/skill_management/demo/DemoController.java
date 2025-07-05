package com.example.skill_management.demo;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@PreAuthorize("hasAnyRole('TechnicalManager', 'QualityManager')")
@RequestMapping("/skill-management/demo-controller")
@Hidden
public class DemoController {

  @GetMapping
  public Mono<ResponseEntity<String>> sayHello() {
    return Mono.just(ResponseEntity.ok("Hello PFA 2025"));
  }

}

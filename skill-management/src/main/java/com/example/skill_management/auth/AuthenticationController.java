package com.example.skill_management.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/skill-management/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

  private final AuthenticationService service;



  @PostMapping("/login")
  public Mono<ResponseEntity<AuthenticationResponse>> authenticate(
          @RequestBody AuthenticationRequest request
  ) {
    return service.authenticate(request)
            .map(ResponseEntity::ok);
  }

  @PostMapping("/refresh-token")
  public Mono<Void> refreshToken(
          ServerHttpRequest request,
          ServerHttpResponse response
  ) {
    return service.refreshToken(request, response);
  }
}

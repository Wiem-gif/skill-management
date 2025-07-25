package com.example.skill_management.demo;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.exception.UserNotFoundException;
import com.example.skill_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing user-related endpoints:
 * - Create user
 * - Delete user
 */
@RestController
@RequestMapping("/skill-management/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('write_user')")
    public Mono<ResponseEntity<String>> createUser(@RequestBody CreateUserRequest request,
                                                   Authentication authentication) {
        String createdBy = authentication.getName();
        return userService.createUser(request, createdBy)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("User created successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_user')")
    public Mono<ResponseEntity<Map<String, Object>>> deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", "SMGT-0006");
                    response.put("message", "User deleted successfully");
                    response.put("status", 204);
                    return ResponseEntity.status(204).body(response);
                }))
                .onErrorResume(UserNotFoundException.class, ex -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", ex.getCode());
                    response.put("message", ex.getMessage());
                    response.put("status", ex.getHttpStatus());
                    return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(response));
                })
                .onErrorResume(Exception.class, ex -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", "SMGT-0000");
                    response.put("message", ex.getMessage());
                    response.put("status", 500);
                    return Mono.just(ResponseEntity.status(500).body(response));
                });
    }

}

package com.example.skill_management.demo;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.service.UserService;
import lombok.RequiredArgsConstructor;

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
    public Mono<String> createUser(@RequestBody CreateUserRequest request, Authentication authentication) {
        String createdBy = authentication.getName();
        return userService.createUser(request, createdBy)
                .thenReturn("User created successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_user')")
    public Mono<ResponseEntity<Map<String, Object>>> deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "User deleted successfully");
                    return ResponseEntity.ok(response);
                }));
    }
}

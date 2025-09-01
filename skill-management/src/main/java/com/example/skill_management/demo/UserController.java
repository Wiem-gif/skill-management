package com.example.skill_management.demo;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.exception.UserNotFoundException;
import com.example.skill_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/skill-management/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API to manage user")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('write_user')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<String>> createUser(@RequestBody CreateUserRequest request,
                                                   Authentication authentication) {
        String createdBy = authentication.getName();
        return userService.createUser(request, createdBy)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("User created successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_user')")
    @Operation(summary = "Restricted to Admin")
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

    @GetMapping("/list")
    public Mono<ResponseEntity<Map<String, Object>>> getAllUsers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return userService.countAllUsers()
                .flatMap(total -> userService.getAllUsers(offset, limit) // <-- renvoie UserResponseDTO
                        .collectList()
                        .map(users -> {
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("total", total);
                            response.put("offset", offset);
                            response.put("limit", limit);
                            response.put("data", users);
                            return ResponseEntity.ok(response);
                        })
                )
                .onErrorResume(Exception.class, ex -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", "SMGT-0000");
                    response.put("message", ex.getMessage());
                    response.put("status", 500);
                    return Mono.just(ResponseEntity.status(500).body(response));
                });
    }


}

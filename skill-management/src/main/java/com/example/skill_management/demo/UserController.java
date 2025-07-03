package com.example.skill_management.demo;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skill-management/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('write_user')")
    public String createUser(@RequestBody CreateUserRequest request, Authentication authentication) {
        String createdBy = authentication.getName();
        userService.createUser(request, createdBy);
        return "User created successfully";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_user')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // 204
    }
}


package com.example.skill_management.service;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.model.User;
import com.example.skill_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(CreateUserRequest request, String createdBy) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdBy(createdBy)
                .creationDate(LocalDateTime.now())
                .status(true)
                .build();

        userRepository.save(user);
    }
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.isProtected()) {
            throw new IllegalStateException("Cannot delete protected user with id: " + id);
        }

        userRepository.delete(user);
    }
}

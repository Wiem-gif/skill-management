package com.example.skill_management.service;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.model.User;
import com.example.skill_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<Void> createUser(CreateUserRequest request, String createdBy) {
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Email already in use"));
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

                    return userRepository.save(user).then();
                });
    }

    public Mono<Void> deleteUser(Integer id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)))
                .flatMap(user -> {
                    if (user.isProtected()) {
                        return Mono.error(new IllegalStateException("Cannot delete protected user with id: " + id));
                    }
                    return userRepository.delete(user);
                });
    }
}

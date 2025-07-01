package com.example.skill_management.controller;

import com.example.skill_management.dto.UserRequest;
import com.example.skill_management.dto.UserResponse;
import com.example.skill_management.model.User;
import com.example.skill_management.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/skill-management/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('write_user')")
    public Mono<ResponseEntity<UserResponse>> createUser(@RequestBody UserRequest userRequest,
                                                         @AuthenticationPrincipal UserDetails currentUser) {
        return userRepository.existsByEmail(userRequest.email())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                    }

                    User newUser = new User();
                    newUser.setFirstname(userRequest.firstname());
                    newUser.setLastname(userRequest.lastname());
                    newUser.setEmail(userRequest.email());
                    newUser.setPassword(passwordEncoder.encode(userRequest.password()));
                    newUser.setRole(userRequest.role());
                    newUser.setCreatedBy(currentUser.getUsername());
                    newUser.setCreationDate(LocalDateTime.now());
                    newUser.setStatus(true);
                    newUser.setProtectedFlag(false);

                    return userRepository.save(newUser)
                            .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED)
                                    .body(new UserResponse(
                                            savedUser.getId(),
                                            savedUser.getFirstname(),
                                            savedUser.getLastname(),
                                            savedUser.getEmail(),
                                            savedUser.getRole(),
                                            savedUser.isStatus(),
                                            savedUser.isProtectedFlag()
                                    )));
                });
    }

    /* @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_user')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    if (user.isProtectedFlag()) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return userRepository.delete(user)
                            .then(Mono.just(ResponseEntity.noContent().build()));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build());
    }*/


}

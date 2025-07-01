package com.example.skill_management.repository;

import com.example.skill_management.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
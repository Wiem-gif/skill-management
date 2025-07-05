package com.example.skill_management.repository;

import com.example.skill_management.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
}

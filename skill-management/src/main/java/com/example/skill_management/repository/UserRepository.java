package com.example.skill_management.repository;

import com.example.skill_management.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
    @Query("SELECT * FROM users ORDER BY id LIMIT :limit OFFSET :offset")
    Flux<User> findAllWithPagination(int offset, int limit);

}

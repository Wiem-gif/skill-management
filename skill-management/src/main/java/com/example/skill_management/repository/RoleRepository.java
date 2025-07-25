package com.example.skill_management.repository;

import com.example.skill_management.model.RoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<RoleEntity, Long> {
    Mono<RoleEntity> findByName(String name);
}

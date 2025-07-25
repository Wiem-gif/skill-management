package com.example.skill_management.repository;

import com.example.skill_management.model.PermissionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PermissionRepository extends ReactiveCrudRepository<PermissionEntity, Long> {
    Mono<PermissionEntity> findByName(String name);
}

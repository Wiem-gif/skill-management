package com.example.skill_management.repository;

import com.example.skill_management.model.Grade;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface GradeRepository extends ReactiveCrudRepository<Grade, Long> {
    Mono<Grade> findByCode(String code);
}


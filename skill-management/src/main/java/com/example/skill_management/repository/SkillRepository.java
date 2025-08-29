package com.example.skill_management.repository;

import com.example.skill_management.model.Skill;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SkillRepository extends ReactiveCrudRepository<Skill, Long> {
    Flux<Skill> findBySkillCategoryId(Long categoryId);
    Mono<Skill> findByName(String name);
    Mono<Void> deleteBySkillCategoryId(Long categoryId);


    Mono<Skill> findByNameIgnoreCase(String name);
}


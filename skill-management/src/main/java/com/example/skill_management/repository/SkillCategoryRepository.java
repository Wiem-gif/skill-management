package com.example.skill_management.repository;

import com.example.skill_management.model.SkillCategory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SkillCategoryRepository extends ReactiveCrudRepository<SkillCategory, Long> {
    Mono<SkillCategory> findByName(String name);
    Mono<SkillCategory> findByNameIgnoreCase(String name);
}

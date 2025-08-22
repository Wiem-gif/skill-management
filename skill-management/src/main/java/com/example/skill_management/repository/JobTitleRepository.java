package com.example.skill_management.repository;

import com.example.skill_management.model.JobTitle;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface JobTitleRepository extends ReactiveCrudRepository<JobTitle, Long> {
    Mono<JobTitle> findByName(String name);
}

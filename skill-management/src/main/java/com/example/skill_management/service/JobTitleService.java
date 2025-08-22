package com.example.skill_management.service;

import com.example.skill_management.model.JobTitle;
import com.example.skill_management.repository.JobTitleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class JobTitleService {

    private final JobTitleRepository repository;

    public JobTitleService(JobTitleRepository repository) {
        this.repository = repository;
    }

    public Mono<Object> createJobTitle(String name, String description) {
        return repository.findByName(name)
                .flatMap(existing -> Mono.error(new RuntimeException("JobTitle already exists")))
                .switchIfEmpty(
                        repository.save(
                                JobTitle.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        )
                );
    }

    public Flux<JobTitle> getAllJobTitles() {
        return repository.findAll();
    }
}

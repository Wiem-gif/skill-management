package com.example.skill_management.demo;


import com.example.skill_management.model.JobTitle;
import com.example.skill_management.service.JobTitleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/job-titles")
@AllArgsConstructor
@Tag(name = "JobTitle  Management", description = "API to manage JobTitle  ")
public class JobTitleController {

    private final JobTitleService service;

    @PostMapping
    public Mono<ResponseEntity<Object>> createJobTitle(@RequestBody JobTitleRequest request) {
        return service.createJobTitle(request.getName(), request.getDescription())
                .map(saved -> ResponseEntity.ok(saved))
                .onErrorResume(e -> {
                    // Vérifie si c’est l’exception de doublon
                    if (e.getMessage().contains("already exists")) {
                        return Mono.just(ResponseEntity
                                .badRequest()
                                .body(Map.of("message", e.getMessage())));
                    }
                    // Pour toute autre erreur
                    return Mono.just(ResponseEntity.status(500)
                            .body(Map.of("message", "Internal server error")));
                });
    }
    @GetMapping
    public Mono<ResponseEntity<List<JobTitle>>> getAllJobTitles() {
        return service.getAllJobTitles()
                .collectList()
                .map(list -> ResponseEntity.ok(list))
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }


    @Data
    public static class JobTitleRequest {
        private String name;
        private String description;
    }
}

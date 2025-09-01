package com.example.skill_management.demo;

import com.example.skill_management.service.JobTitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/job-titles")
@AllArgsConstructor
@Tag(name = "JobTitle  Management", description = "API to manage JobTitle  ")
public class JobTitleController {

    private final JobTitleService service;

    @PostMapping
    @PreAuthorize("hasAuthority('write_jobTitle')")
    @Operation(summary = "Restricted to Admin")
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

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('read_jobTitle')")
    public Mono<ResponseEntity<Map<String, Object>>> getAllJobTitles(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return service.countAllJobTitles()
                .flatMap(total -> service.getAllJobTitles()
                        .skip(offset)
                        .take(limit)
                        .collectList()
                        .map(jobTitles -> {
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("total", total);
                            response.put("offset", offset);
                            response.put("limit", limit);
                            response.put("data", jobTitles);
                            return ResponseEntity.ok(response);
                        })
                )

                .onErrorResume(ex -> {
                    Map<String, Object> response = Map.of(
                            "code", "SMGT-0000",
                            "message", ex.getMessage(),
                            "status", 500
                    );
                    return Mono.just(ResponseEntity.status(500).body(response));
                });
    }

    @Data
    public static class JobTitleRequest {
        private String name;
        private String description;
    }
}

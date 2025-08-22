package com.example.skill_management.demo;

import com.example.skill_management.dto.ApiUpdateCategoyResponse;
import com.example.skill_management.exception.SkillNotFoundException;
import com.example.skill_management.model.Skill;
import com.example.skill_management.service.SkillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
@Tag(name = "Skill Management", description = "API to manage skills")
public class SkillController {

    private final SkillService service;

    @GetMapping
    public Flux<Skill> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getById(@PathVariable Long id) {
        return service.findById(id)
                .<ResponseEntity<Object>>map(skill -> ResponseEntity.ok(skill))
                .onErrorResume(SkillNotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(404)
                                .body(Map.of("message", e.getMessage())))
                );
    }

    @PostMapping
    public Mono<Skill> create(@RequestBody Skill skill) {
        return service.create(skill);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable Long id, @RequestBody Skill updated) {
        return service.update(id, updated)
                .map(saved -> ResponseEntity.ok((Object) saved))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Skill not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Skill with id " + id + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Skill not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Skill with id " + id + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }

    @GetMapping("/category/{categoryId}")
    public Flux<Skill> getByCategory(@PathVariable Long categoryId) {
        return service.findByCategory(categoryId)
                .switchIfEmpty(Mono.error(new SkillNotFoundException()));
    }
    @PutMapping("/{id}/category")
    public Mono<ResponseEntity<ApiUpdateCategoyResponse<Skill>>> updateSkillCategory(@PathVariable Long id,
                                                                                              @RequestBody Map<String, String> body) {
        String categoryName = body.get("name");
        if (categoryName == null || categoryName.isBlank()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(ApiUpdateCategoyResponse.<Skill>builder()
                            .message("Category name must be provided")
                            .build()));
        }

        return service.updateSkillCategory(id, categoryName)
                .map(skill -> ResponseEntity.ok(
                        ApiUpdateCategoyResponse.<Skill>builder()
                                .message("Skill updated successfully")
                                .data(skill)
                                .build()))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Skill not found")) {
                        return Mono.just(ResponseEntity.status(404)
                                .body(ApiUpdateCategoyResponse.<Skill>builder()
                                        .message(e.getMessage())
                                        .build()));
                    }
                    return Mono.just(ResponseEntity.status(500)
                            .body(ApiUpdateCategoyResponse.<Skill>builder()
                                    .message("An error occurred: " + e.getMessage())
                                    .build()));
                });
    }



}

package com.example.skill_management.demo;

import com.example.skill_management.exception.GradeNotFoundException;
import com.example.skill_management.model.Grade;
import com.example.skill_management.service.GradeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor

@Tag(name = "Grade Management", description = "API to manage employee grade")
public class GradeController {

    private final GradeService service;

    @GetMapping
    public Flux<Grade> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getById(@PathVariable Long id) {
        return service.findById(id)
                .<ResponseEntity<Object>>map(grade -> ResponseEntity.ok(grade))
                .onErrorResume(GradeNotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(404)
                                .body(Map.of("message", e.getMessage())))
                );
    }


    @PostMapping
    public Mono<Grade> create(@RequestBody Grade grade) {
        return service.create(grade);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable Long id, @RequestBody Grade updated) {
        return service.update(id, updated)
                .map(saved -> ResponseEntity.ok((Object) saved))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Grade not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Grade with id " + id + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_grade')")
    public Mono<ResponseEntity<Object>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Grade not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Grade with id " + id + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }

}

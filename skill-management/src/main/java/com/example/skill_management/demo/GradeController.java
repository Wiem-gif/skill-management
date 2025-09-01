package com.example.skill_management.demo;

import com.example.skill_management.exception.GradeNotFoundException;
import com.example.skill_management.model.Grade;
import com.example.skill_management.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor

@Tag(name = "Grade Management", description = "API to manage employee grade")
public class GradeController {

    private final GradeService service;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('read_grade')")
    public Mono<ResponseEntity<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return service.countAllGrades()
                .flatMap(total -> service.findAll()
                        .skip(offset)
                        .take(limit)
                        .collectList()
                        .map(grades -> {
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("total", total);
                            response.put("offset", offset);
                            response.put("limit", limit);
                            response.put("data", grades);
                            return ResponseEntity.ok(response);
                        })
                )
                .onErrorResume(ex -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("code", "SMGT-0000");
                    error.put("message", ex.getMessage());
                    error.put("status", 500);
                    return Mono.just(ResponseEntity.status(500).body(error));
                });
    }



    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('read_grade')")
    public Mono<ResponseEntity<Object>> getById(@PathVariable Long id) {
        return service.findById(id)
                .<ResponseEntity<Object>>map(grade -> ResponseEntity.ok(grade))
                .onErrorResume(GradeNotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(404)
                                .body(Map.of("message", e.getMessage())))
                );
    }


    @PostMapping
    @PreAuthorize("hasAuthority('write_grade')")
    @Operation(summary = "Restricted to Admin")
    public Mono<Grade> create(@RequestBody Grade grade) {
        return service.create(grade);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_grade')")
    @Operation(summary = "Restricted to Admin")
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
@Operation(summary = "Restricted to Admin")
public Mono<ResponseEntity<Map<String, Object>>> delete(@PathVariable Long id) {
    return service.delete(id)
            .then(Mono.fromSupplier(() -> {
                Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Grade deleted successfully"
                );
                return ResponseEntity.ok(response);
            }));
}



}

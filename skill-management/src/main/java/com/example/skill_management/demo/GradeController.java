package com.example.skill_management.demo;

import com.example.skill_management.exception.GradeNotFoundException;
import com.example.skill_management.model.Grade;
import com.example.skill_management.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor

@Tag(name = "Grade Management", description = "API to manage employee grade")
public class GradeController {

    private final GradeService service;

    @GetMapping
    @PreAuthorize("hasAuthority('read_grade')")


    public Mono<ResponseEntity<List<Grade>>> getAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return service.findAll()
                .skip(offset)
                .take(limit)
                .collectList()
                .map(ResponseEntity::ok);
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
    public Mono<Grade> create(@RequestBody Grade grade) {
        return service.create(grade);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_grade')")
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

//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('delete_grade')")
//    public Mono<ResponseEntity<Object>> delete(@PathVariable Long id) {
//        return service.delete(id)
//                .then(Mono.just(ResponseEntity.noContent().build()))
//                .onErrorResume(GradeNotFoundException.class, e ->
//                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
//                                .body(Map.of("error", "Grade with id " + id + " not found")))
//                )
//                .onErrorResume(e -> {
//                    // Log de l'erreur pour debug
//                    e.printStackTrace();
//                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body(Map.of("error", "Internal server error")));
//                });
//    }
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('delete_grade')")
public Mono<ResponseEntity<Map<String, Object>>> delete(@PathVariable Long id) {
    return service.delete(id)
            .then(Mono.fromSupplier(() -> {
                Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Grade deleted successfully"
                );
                return ResponseEntity.ok(response); // HTTP 200 avec JSON
            }));
}



}

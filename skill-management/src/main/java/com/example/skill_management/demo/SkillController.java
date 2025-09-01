package com.example.skill_management.demo;



import com.example.skill_management.exception.SkillNotFoundException;
import com.example.skill_management.model.Skill;
import com.example.skill_management.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
@Tag(name = "Skill Management", description = "API to manage skills")
public class SkillController {

    private final SkillService service;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN','QualityManager','TechnicalManager')")
    @Operation(summary = "Get all skills")
    @ApiResponse(responseCode = "200", description = "List of skills",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Skill.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden - Access Denied",
            content = @Content(mediaType = "application/json"))
    public Mono<ResponseEntity<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return service.countAllSkills()
                .flatMap(total -> service.findAllWithCategoryName()
                        .skip(offset)   // pagination offset
                        .take(limit)    // pagination limit
                        .collectList()
                        .map(data -> {
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("total", total);
                            response.put("offset", offset);
                            response.put("limit", limit);
                            response.put("data", data);
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
    public Mono<ResponseEntity<Object>> getById(@PathVariable Long id) {
        return service.findById(id)
                .<ResponseEntity<Object>>map(dto -> ResponseEntity.ok(dto))
                .onErrorResume(SkillNotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(404)
                                .body(Map.of("message", e.getMessage())))
                );
    }


    @PostMapping
    @PreAuthorize("hasAuthority('write_skill')")
    @Operation(summary = "Restricted to Admin")
    public Mono<Skill> create(@RequestBody Skill skill) {
        return service.create(skill);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_skill')")
    @Operation(summary = "Restricted to Admin")
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
    @PreAuthorize("hasAuthority('delete_skill')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Map<String, Object>>> deleteSkill(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("status", "success");
                    response.put("message", "Skill and associated employee skills deleted successfully");
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    if (e.getMessage().contains("Skill not found")) {
                        error.put("status", "error");
                        error.put("message", "Skill with id " + id + " not found");
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
                    }
                    error.put("status", "error");
                    error.put("message", "Internal server error");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
                });
    }



}

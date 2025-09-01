package com.example.skill_management.demo;

import com.example.skill_management.model.SkillCategory;
import com.example.skill_management.service.SkillCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/skill-category")
@RequiredArgsConstructor
@Tag(name = "Skill Category Management", description = "API to manage skill category")
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;
    @PreAuthorize("hasAuthority('create_category')")
    @PostMapping
    @Operation(summary = "Restricted to Admin")
    public Mono<SkillCategory> create(@RequestBody SkillCategory skillCategory) {
        return skillCategoryService.createSkillCategory(skillCategory);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN','QualityManager','TechnicalManager')")
    @Operation(summary = "Get all categories")
    @ApiResponse(responseCode = "200", description = "List of categories",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = SkillCategory.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden - Access Denied",
            content = @Content(mediaType = "application/json"))
    public Mono<ResponseEntity<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return skillCategoryService.countAllSkillCategories()
                .flatMap(total -> skillCategoryService.getAllSkillCategories()
                        .skip(offset)
                        .take(limit)
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


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_skill_category')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Object>> update(@PathVariable Long id, @RequestBody SkillCategory skillCategory) {
        return skillCategoryService.updateSkillCategory(id, skillCategory)
                .map(saved -> ResponseEntity.ok((Object) saved))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Category not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Category with id " + id + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_skill_category')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Map<String, Object>>> delete(@PathVariable Long id) {
        return skillCategoryService.deleteSkillCategory(id)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = Map.of(
                            "status", "success",
                            "message", "Skill category deleted successfully"
                    );
                    return ResponseEntity.ok(response); // HTTP 200 avec JSON
                }))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Category not found")) {

                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Category with id " + id + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }


}

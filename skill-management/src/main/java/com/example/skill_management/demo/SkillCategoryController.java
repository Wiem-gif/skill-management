package com.example.skill_management.demo;



import com.example.skill_management.model.SkillCategory;
import com.example.skill_management.service.SkillCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/skill-category")
@RequiredArgsConstructor
@Tag(name = "Skill Category Management", description = "API to manage skill category")
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;
    @PreAuthorize("hasAuthority('create_category')")
    @PostMapping
    public Mono<SkillCategory> create(@RequestBody SkillCategory skillCategory) {
        return skillCategoryService.createSkillCategory(skillCategory);
    }

    @GetMapping
    public Flux<SkillCategory> getAll() {
        return skillCategoryService.getAllSkillCategories();
    }

    @PutMapping("/{id}")
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
    public Mono<ResponseEntity<Object>> delete(@PathVariable Long id) {
        return skillCategoryService.deleteSkillCategory(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
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

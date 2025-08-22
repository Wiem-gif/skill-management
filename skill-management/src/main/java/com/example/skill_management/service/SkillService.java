package com.example.skill_management.service;

import com.example.skill_management.exception.SkillNotFoundException;
import com.example.skill_management.model.Skill;
import com.example.skill_management.model.SkillCategory;
import com.example.skill_management.repository.SkillCategoryRepository;
import com.example.skill_management.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository repository;
    private final SkillCategoryRepository skillCategoryRepository;

    public Flux<Skill> findAll() {
        return repository.findAll();
    }

    public Mono<Skill> findById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new SkillNotFoundException()));
    }


    public Mono<Skill> create(Skill skill) {
        return repository.save(skill);
    }

    public Mono<Skill> update(Long id, Skill updated) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Skill not found")))
                .flatMap(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    existing.setSkillCategoryId(updated.getSkillCategoryId());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Skill not found")))
                .flatMap(existing -> repository.delete(existing));
    }


    public Flux<Skill> findByCategory(Long categoryId) {
        return repository.findBySkillCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new SkillNotFoundException()));
    }
    // 🔹 Crée ou récupère une catégorie par nom
    public Mono<SkillCategory> getOrCreateCategoryByName(String name) {
        return skillCategoryRepository.findByNameIgnoreCase(name)
                .switchIfEmpty(skillCategoryRepository.save(new SkillCategory(null, name)));
    }

    // 🔹 Mise à jour de la catégorie d'un skill
    // Dans SkillService
    public Mono<Skill> updateSkillCategory(Long skillId, String categoryName) {
        return repository.findById(skillId)
                .switchIfEmpty(Mono.error(new RuntimeException("Skill not found")))
                .flatMap(skill ->
                        skillCategoryRepository.findByNameIgnoreCase(categoryName)
                                .switchIfEmpty(skillCategoryRepository.save(
                                        SkillCategory.builder().name(categoryName).build()))
                                .flatMap(cat -> {
                                    skill.setSkillCategoryId(cat.getId());
                                    return repository.save(skill);
                                })
                );
    }


}

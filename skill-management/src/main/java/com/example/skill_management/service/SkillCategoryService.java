package com.example.skill_management.service;



import com.example.skill_management.model.SkillCategory;
import com.example.skill_management.repository.SkillCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SkillCategoryService {

    private final SkillCategoryRepository skillCategoryRepository;

    public Mono<SkillCategory> createSkillCategory(SkillCategory skillCategory) {
        return skillCategoryRepository.save(skillCategory);
    }

    public Flux<SkillCategory> getAllSkillCategories() {
        return skillCategoryRepository.findAll();
    }

    public Mono<SkillCategory> updateSkillCategory(Long id, SkillCategory updatedCategory) {
        return skillCategoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Category not found")))
                .flatMap(existing -> {
                    existing.setName(updatedCategory.getName());
                    return skillCategoryRepository.save(existing);
                });
    }


    public Mono<Void> deleteSkillCategory(Long id) {
        return skillCategoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Category not found")))
                .flatMap(existing -> skillCategoryRepository.delete(existing));
    }
}
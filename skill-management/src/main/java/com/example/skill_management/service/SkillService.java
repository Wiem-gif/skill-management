package com.example.skill_management.service;

import com.example.skill_management.dto.GetAllSkillResponse;
import com.example.skill_management.exception.SkillNotFoundException;
import com.example.skill_management.model.Skill;
import com.example.skill_management.model.SkillCategory;
import com.example.skill_management.repository.EmployeeSkillRepository;
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
    private final EmployeeSkillRepository employeeSkillRepository;

    public Flux<GetAllSkillResponse> findAllWithCategoryName() {
        return repository.findAll()
                .flatMap(skill ->
                        skillCategoryRepository.findById(skill.getSkillCategoryId())
                                .map(category -> new GetAllSkillResponse(
                                        skill.getId(),
                                        skill.getSkillCategoryId(),
                                        category.getName(),
                                        skill.getName(),
                                        skill.getDescription()

                                ))
                );
    }


    public Mono<GetAllSkillResponse> findById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new SkillNotFoundException()))
                .flatMap(skill ->
                        skillCategoryRepository.findById(skill.getSkillCategoryId())
                                .map(category -> new GetAllSkillResponse(
                                        skill.getId(),
                                        skill.getSkillCategoryId(),
                                        category.getName(),   // categoryName
                                        skill.getName(),
                                        skill.getDescription()
                                ))
                );
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
                .flatMap(skill ->
                        employeeSkillRepository.deleteBySkillId(skill.getId()) // Supprime les associations
                                .then(repository.delete(skill)) // Supprime le skill
                );
    }


//    public Flux<Skill> findByCategory(Long categoryId) {
//        return repository.findBySkillCategoryId(categoryId)
//                .switchIfEmpty(Mono.error(new SkillNotFoundException()));
//    }
    // 🔹 Cherche ou crée une catégorie par nom
    public Mono<SkillCategory> getOrCreateCategoryByName(String name) {
        return skillCategoryRepository.findByNameIgnoreCase(name)
                .switchIfEmpty(
                        skillCategoryRepository.save(
                                SkillCategory.builder().name(name).build()
                        )
                );
    }

    // 🔹 Mise à jour de la catégorie d'un skill
    public Mono<Skill> updateSkillCategory(Long skillId, String categoryName) {
        return repository.findById(skillId)
                .switchIfEmpty(Mono.error(new RuntimeException("Skill not found")))
                .flatMap(skill ->
                        getOrCreateCategoryByName(categoryName) // ✅ centralise ici
                                .flatMap(category -> {
                                    skill.setSkillCategoryId(category.getId());
                                    return repository.save(skill);
                                })
                );
    }


}

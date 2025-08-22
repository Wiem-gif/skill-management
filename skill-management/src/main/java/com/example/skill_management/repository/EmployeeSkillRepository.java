package com.example.skill_management.repository;

import com.example.skill_management.model.EmployeeSkill;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeSkillRepository extends ReactiveCrudRepository<EmployeeSkill, Long> {

    // Anciennes méthodes basées sur employeeMatricule (compatibilité)
//    Flux<EmployeeSkill> findByEmployeeMatricule(String employeeMatricule);
//    Mono<EmployeeSkill> findByEmployeeMatriculeAndSkillId(String employeeMatricule, Long skillId);

    // Nouvelles méthodes basées sur employeeId
    Flux<EmployeeSkill> findByEmployeeId(Long employeeId);
    @Query("UPDATE employee_skill SET current_level = :currentLevel WHERE employee_id = :employeeId AND skill_id = :skillId")
    Mono<Integer> updateSkillLevel(Long employeeId, Long skillId, String currentLevel);
    @Modifying
    @Query("UPDATE employee_skill es " +
            "SET current_level = :currentLevel " +
            "FROM employee e " +
            "WHERE es.employee_id = e.id AND e.matricule = :matricule AND es.skill_id = :skillId")
    Mono<Integer> updateSkillLevelByMatricule(String matricule, Long skillId, String currentLevel);
    Mono<EmployeeSkill> findByEmployeeIdAndSkillId(Long employeeId, Long skillId);
    Mono<Void> deleteByEmployeeId(Long employeeId);

    // Recherche par skill
    Flux<EmployeeSkill> findBySkillId(Long skillId);
}

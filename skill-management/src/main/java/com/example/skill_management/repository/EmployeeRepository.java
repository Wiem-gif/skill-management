package com.example.skill_management.repository;

import com.example.skill_management.model.Employee;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface EmployeeRepository extends R2dbcRepository<Employee, Long> {



    @Query("SELECT * FROM employee WHERE LOWER(matricule) = LOWER(:matricule)")
    Mono<Employee> findByMatriculeIgnoreCase(@Param("matricule") String matricule);
    Mono<Employee> findByMatricule(String matricule);




    Mono<Employee> findByEmail(String email);


    Mono<Employee> findByCin(String cin);
    Mono<Employee> findById(Long id);
    Mono<Employee> findByFirstnameAndLastname(String firstname, String lastname);

}
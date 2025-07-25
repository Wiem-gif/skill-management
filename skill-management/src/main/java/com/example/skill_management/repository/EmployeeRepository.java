package com.example.skill_management.repository;

import com.example.skill_management.model.Employee;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Mono;

public interface EmployeeRepository extends R2dbcRepository<Employee, String> {


    Mono<Employee> findByMatricule(String matricule);

    Mono<Employee> findByEmail(String email);


    Mono<Employee> findByCin(String cin);
}
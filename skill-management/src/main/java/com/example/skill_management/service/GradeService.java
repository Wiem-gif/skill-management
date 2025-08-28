package com.example.skill_management.service;

import com.example.skill_management.exception.GradeNotFoundException;
import com.example.skill_management.model.Grade;
import com.example.skill_management.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository repository;

    public Flux<Grade> findAll() {
        return repository.findAll();
    }

    public Mono<Grade> findById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new GradeNotFoundException(String.valueOf(id))));
    }


    public Mono<Grade> create(Grade grade) {
        return repository.save(grade);
    }

    public Mono<Grade> update(Long id, Grade updated) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Grade not found")))
                .flatMap(existing -> {
                    existing.setCategory(updated.getCategory());
                    existing.setCode(updated.getCode());
                    return repository.save(existing);
                });
    }

//    public Mono<Void> delete(Long id) {
//        return repository.findById(id)
//                .switchIfEmpty(Mono.error(new RuntimeException("Grade not found")))
//                .flatMap(existing -> repository.delete(existing));
//    }
public Mono<Void> delete(Long id) {
    return repository.findById(id)
            .switchIfEmpty(Mono.error(new GradeNotFoundException(id.toString())))
            .flatMap(repository::delete); // Mono<Void>
}

}

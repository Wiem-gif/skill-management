package com.example.skill_management.token;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenRepository extends ReactiveCrudRepository<Token, Integer> {


  @Query("SELECT * FROM token WHERE user_id = :userId AND (expired = false OR revoked = false)")
  Flux<Token> findAllValidTokenByUser(Integer userId);


  Mono<Token> findByToken(String token);
}

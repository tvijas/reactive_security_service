package com.example.reactive.security.security.repo;

import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.security.models.entities.tokens.TokensEntity;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TokensRepo {
    Mono<TokensEntity> findByUsers(UserEntity userEntity);
//    @Modifying
//    @Transactional
//    @Query("DELETE FROM tokens WHERE users.id = :userId")
//    Mono<Void> deleteByUserId(UUID userId);

    Mono<TokensEntity> save(TokensEntity tokensEntity);
    Mono<TokensEntity> findById(UUID uuid);
}

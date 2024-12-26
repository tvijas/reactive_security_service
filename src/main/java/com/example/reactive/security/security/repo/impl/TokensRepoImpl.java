package com.example.reactive.security.security.repo.impl;

import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.security.models.entities.tokens.AccessTokenEntity;
import com.example.reactive.security.security.models.entities.tokens.RefreshTokenEntity;
import com.example.reactive.security.security.models.entities.tokens.TokensEntity;
import com.example.reactive.security.security.repo.AccessTokenRepo;
import com.example.reactive.security.security.repo.RefreshTokenRepo;
import com.example.reactive.security.security.repo.TokensRepo;
import com.example.reactive.security.security.repo.sql.TokensSQL;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TokensRepoImpl implements TokensRepo {
    private final DatabaseClient databaseClient;
    private final AccessTokenRepo accessTokenRepo;
    private final RefreshTokenRepo refreshTokenRepo;

    @Override
    public Mono<TokensEntity> findByUsers(UserEntity userEntity) {
        return databaseClient.sql(TokensSQL.SELECT_BY_USERS_ID)
                .bind("userId", userEntity.getId())
                .map(row -> TokensEntity.builder()
                        .id(row.get("token_id", UUID.class))
                        .updatedAt(row.get("updated_at", Instant.class))
                        .accessToken(AccessTokenEntity.fromRow(row))
                        .refreshToken(RefreshTokenEntity.fromRow(row))
                        .users(userEntity)
                        .build())
                .one();
    }

    @Override
    @Transactional
    public Mono<TokensEntity> save(TokensEntity tokensEntity) {
        return Mono.zip(accessTokenRepo.save(tokensEntity.getAccessToken()),
                        refreshTokenRepo.save(tokensEntity.getRefreshToken()))
                .flatMap(tuple -> {
                    if (tokensEntity.getId() == null)
                        tokensEntity.setId(UUID.randomUUID());

                    return databaseClient.sql(TokensSQL.SAVE_PERSIST)
                            .bind("id", tokensEntity.getId())
                            .bind("accessTokenId", tuple.getT1().getId())
                            .bind("refreshTokenId", tuple.getT2().getId())
                            .bind("updatedAt", tokensEntity.getUpdatedAt())
                            .bind("usersId", tokensEntity.getUsers().getId())
                            .then().thenReturn(tokensEntity.toBuilder()
                                    .accessToken(tuple.getT1())
                                    .refreshToken(tuple.getT2())
                                    .build());
                });
    }

    @Override
    public Mono<TokensEntity> findById(UUID uuid) {
        return databaseClient.sql(TokensSQL.SELECT_BY_ID)
                .bind("id", uuid)
                .map(TokensEntity::fromRow)
                .one();
    }
}

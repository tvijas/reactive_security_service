package com.example.reactive.security.security.service.jwt;

import com.example.reactive.security.exceptions.BasicException;
import com.example.reactive.security.foruser.UserEntity;
import com.example.reactive.security.security.models.entities.tokens.AccessTokenEntity;
import com.example.reactive.security.security.models.entities.tokens.RefreshTokenEntity;
import com.example.reactive.security.security.models.entities.tokens.TokensEntity;
import com.example.reactive.security.security.repo.TokensRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TokensRepo tokensRepo;

    @Transactional
    public Mono<TokensEntity> saveRefreshedTokenPair(Instant expiresAt,
                                                     Instant updatedAt,
                                                     Instant accessExpiration,
                                                     Instant refreshExpiration,
                                                     UserEntity users) {
        return tokensRepo.findByUsers(users)
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("user", "There are no linked tokens to you"), HttpStatus.NOT_FOUND
                )))
                .filter(tokensEntity -> tokensEntity.getUpdatedAt().plusSeconds(60).toEpochMilli() <= updatedAt.toEpochMilli())
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("request", "too many refresh requests"), HttpStatus.TOO_MANY_REQUESTS
                )))
                .filter(tokensEntity -> tokensEntity.getRefreshToken().getExpiresAt()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .equals(expiresAt))
                .switchIfEmpty(Mono.error(new BasicException(
                        Map.of("refresh_token", "Refresh Token was used! You can use refresh token only once."),
                        HttpStatus.BAD_REQUEST
                )))
                .flatMap(tokensEntity -> {
                    tokensEntity.getAccessToken().setExpiresAt(accessExpiration);
                    tokensEntity.getRefreshToken().setExpiresAt(refreshExpiration);
                    tokensEntity.setUpdatedAt(updatedAt);

                    return tokensRepo.save(tokensEntity);
                });
    }

    @Transactional
    public Mono<TokensEntity> saveGeneratedTokenPair(Instant updatedAt,
                                                     Instant accessExpiration,
                                                     Instant refreshExpiration,
                                                     UserEntity users) {
        return tokensRepo.findByUsers(users)
                .flatMap(existingTokensEntity -> {
                    existingTokensEntity.getAccessToken().setExpiresAt(accessExpiration);
                    existingTokensEntity.getRefreshToken().setExpiresAt(refreshExpiration);
                    existingTokensEntity.setUpdatedAt(updatedAt);

                    return tokensRepo.save(existingTokensEntity);
                })
                .switchIfEmpty(tokensRepo.save(TokensEntity.builder()
                        .accessToken(AccessTokenEntity.builder()
                                .expiresAt(accessExpiration)
                                .build())
                        .refreshToken(RefreshTokenEntity.builder()
                                .expiresAt(refreshExpiration)
                                .build())
                        .updatedAt(updatedAt)
                        .users(users)
                        .build()));
    }
}



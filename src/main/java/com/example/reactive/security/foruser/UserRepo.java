package com.example.reactive.security.foruser;

import com.example.reactive.security.security.models.enums.Provider;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepo extends R2dbcRepository<UserEntity, UUID> {
    Mono<UserEntity> findByEmailAndProvider(String email, Provider provider);

    Mono<Boolean> existsByEmailAndProvider(String email, Provider provider);

    Mono<UserEntity> findByProviderIdAndProvider(String providerId, Provider provider);

    @Modifying
    @Transactional
    @Query("UPDATE users SET is_email_submitted = true WHERE email = :email AND provider = :provider")
    Mono<Integer> updateIsEmailSubmittedByEmailAndProvider(@Param("email") String email, @Param("provider") Provider provider);
}

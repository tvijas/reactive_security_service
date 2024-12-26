package com.example.reactive.security.security.repo;

import com.example.reactive.security.security.models.entities.tokens.RefreshTokenEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface RefreshTokenRepo extends R2dbcRepository<RefreshTokenEntity, UUID> {
}

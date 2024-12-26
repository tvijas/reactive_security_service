package com.example.reactive.security.security.models.entities.tokens;

import com.example.reactive.security.foruser.UserEntity;
import io.r2dbc.spi.Readable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "tokens")
public class TokensEntity {
    @Id
    private UUID id;
    private AccessTokenEntity accessToken;
    private RefreshTokenEntity refreshToken;
    private Instant updatedAt;
    private UserEntity users;

    public static TokensEntity fromRow(Readable row) {
        return TokensEntity.builder()
                .id(row.get("token_id", UUID.class))
                .updatedAt(row.get("updated_at", Instant.class))
                .accessToken(AccessTokenEntity.fromRow(row))
                .refreshToken(RefreshTokenEntity.fromRow(row))
                .users(UserEntity.fromRow(row))
                .build();
    }
}
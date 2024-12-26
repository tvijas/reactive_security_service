package com.example.reactive.security.security.models.entities.tokens;

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

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "refresh_token")
public class RefreshTokenEntity {
    @Id
    private UUID id;
    private Instant expiresAt;

    public static RefreshTokenEntity fromRow(Readable row) {
        return RefreshTokenEntity.builder()
                .id(row.get("refresh_token_id", UUID.class))
                .expiresAt(row.get("refresh_token_expires_at", Instant.class))
                .build();
    }
}

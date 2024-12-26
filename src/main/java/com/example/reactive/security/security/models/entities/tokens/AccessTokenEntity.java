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
@Table(name = "access_token")
public class AccessTokenEntity {
    @Id
    private UUID id;
    private Instant expiresAt;

    public static AccessTokenEntity fromRow(Readable row) {
        return AccessTokenEntity.builder()
                .id(row.get("access_token_id", UUID.class))
                .expiresAt(row.get("access_token_expires_at", Instant.class))
                .build();
    }
}
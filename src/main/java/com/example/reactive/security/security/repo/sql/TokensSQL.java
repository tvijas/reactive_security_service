package com.example.reactive.security.security.repo.sql;

public final class TokensSQL {
    private TokensSQL() {
    }

    public static final String SELECT_BY_USERS_ID = """
            SELECT t.id as token_id, t.updated_at, 
                   a.id as access_token_id, a.expires_at as access_token_expires_at, 
                   r.id as refresh_token_id, r.expires_at as refresh_token_expires_at
            FROM tokens t
            JOIN access_token a ON t.access_token_id = a.id
            JOIN refresh_token r ON t.refresh_token_id = r.id
            WHERE t.users_id = :userId
            """;
    public static final String SAVE_PERSIST = """
            INSERT INTO tokens (id, access_token_id, refresh_token_id, updated_at, users_id)
            VALUES (:id, :accessTokenId, :refreshTokenId, :updatedAt, :usersId)
            """;
    public static final String SELECT_BY_ID = """
            SELECT t.id as token_id, t.updated_at, 
                       a.id as access_token_id, a.expires_at as access_token_expires_at, 
                       r.id as refresh_token_id, r.expires_at as refresh_token_expires_at,
                       u.id as user_id, u.email, u.password, u.provider, u.provider_id, u.last_active_date,
                       u.registration_date, u.is_email_submitted, u.roles
                FROM tokens t
                JOIN access_token a ON t.access_token_id = a.id
                JOIN refresh_token r ON t.refresh_token_id = r.id
                JOIN users u ON t.users_id = u.id
                WHERE t.id = :id
            """;
}

CREATE TABLE users
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(255) UNIQUE        NOT NULL,
    password           VARCHAR(255)               NOT NULL,
    provider           VARCHAR(50)                NOT NULL CHECK (users.provider IN ('LOCAL', 'GOOGLE')),
    provider_id        VARCHAR(255),
    last_active_date   TIMESTAMP,
    registration_date  TIMESTAMP                  NOT NULL,
    is_email_submitted BOOLEAN     DEFAULT false  NOT NULL,
--     tokens_id          UUID,
    roles              VARCHAR(50) DEFAULT 'USER' NOT NULL CHECK (users.roles IN ('USER', 'ADMIN'))
);

CREATE TABLE access_token
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE refresh_token
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE tokens
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    access_token_id  UUID      NOT NULL,
    refresh_token_id UUID      NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    users_id         UUID      NOT NULL,
    CONSTRAINT fk_tokens_access_token FOREIGN KEY (access_token_id) REFERENCES access_token (id) ON DELETE CASCADE,
    CONSTRAINT fk_tokens_refresh_token FOREIGN KEY (refresh_token_id) REFERENCES refresh_token (id) ON DELETE CASCADE,
    CONSTRAINT fk_tokens_users FOREIGN KEY (users_id) REFERENCES users (id)
);

-- ALTER TABLE users
--     ADD CONSTRAINT fk_users_tokens FOREIGN KEY (tokens_id) REFERENCES tokens (id) ON DELETE CASCADE;
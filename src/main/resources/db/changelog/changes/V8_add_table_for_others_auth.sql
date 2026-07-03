CREATE TABLE if not exists socials_auth
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT references users (id) NOT NULL,
    provider    VARCHAR(255)                 NOT NULL,
    provider_id VARCHAR(255)                 NOT NULL,

    CONSTRAINT fk_socials_auth_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_socials_auth_provider_id
        UNIQUE (provider, provider_id)
);

CREATE INDEX idx_socials_auth_user_id ON socials_auth (user_id);
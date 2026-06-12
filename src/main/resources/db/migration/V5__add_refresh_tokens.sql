CREATE TABLE refresh_tokens (
    id           CHAR(36)  NOT NULL,
    user_id      BIGINT    NOT NULL,
    expires_at   DATETIME  NOT NULL,
    revoked      BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_rt_user ON refresh_tokens(user_id);

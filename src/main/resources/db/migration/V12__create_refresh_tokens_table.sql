-- Refresh token store for token rotation and family-based revocation.
-- Each login creates a "family". On refresh, the old token is consumed and a new
-- one is issued in the same family. If a consumed token is replayed, the entire
-- family is revoked (theft detection).

CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token_family VARCHAR(36) NOT NULL COMMENT 'Groups tokens from a single login session',
    token_hash  VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hash of the jti claim',
    is_consumed BOOLEAN      NOT NULL DEFAULT FALSE,
    is_revoked  BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_family (token_family),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires (expires_at),

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

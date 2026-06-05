CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_role_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_deleted_created ON users (deleted, created_at);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),
    cover_image_url VARCHAR(500),
    user_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_posts_author ON posts (user_id);
CREATE INDEX idx_posts_created_at ON posts (created_at);
CREATE INDEX idx_posts_category_created ON posts (category, created_at);
CREATE INDEX idx_posts_deleted_created_id ON posts (deleted, created_at, id);
CREATE FULLTEXT INDEX ft_posts_search ON posts (title, description, content);

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    body TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comments_post ON comments (post_id);
CREATE INDEX idx_comments_user ON comments (user_id);
CREATE INDEX idx_comments_deleted_created ON comments (deleted, created_at);

CREATE TABLE post_likes (
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    used_at DATETIME(6) NULL,
    replaced_by_token_hash VARCHAR(64) NULL,
    created_by_ip VARCHAR(64) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens (expires_at);

CREATE TABLE outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(80) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6) NULL,
    last_error VARCHAR(1000) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_outbox_status_next_attempt ON outbox_events (status, next_attempt_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);

INSERT INTO roles (name)
VALUES ('ROLE_USER'), ('ROLE_ADMIN');

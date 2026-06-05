-- =============================================================================
-- BlogHub Database Schema
-- MySQL 8.0+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS bloghub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bloghub_db;

-- =============================================================================
-- ROLES
-- =============================================================================
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_role_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- USERS
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);

-- =============================================================================
-- USER_ROLES (join table)
-- =============================================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- POSTS
-- =============================================================================
CREATE TABLE IF NOT EXISTS posts (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL,
    content     TEXT         NOT NULL,
    category    VARCHAR(100),
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_posts_author     ON posts (user_id);
CREATE INDEX idx_posts_created_at ON posts (created_at);
CREATE INDEX idx_posts_category   ON posts (category);

-- Full-text search index on title, description, and content
ALTER TABLE posts ADD FULLTEXT INDEX ft_posts_search (title, description, content);

-- =============================================================================
-- COMMENTS
-- =============================================================================
CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT    NOT NULL AUTO_INCREMENT,
    body       TEXT      NOT NULL,
    post_id    BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comments_post ON comments (post_id);
CREATE INDEX idx_comments_user ON comments (user_id);

-- =============================================================================
-- POST_LIKES (join table for likes feature)
-- =============================================================================
CREATE TABLE IF NOT EXISTS post_likes (
    post_id    BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- SEED DATA
-- =============================================================================

-- Default roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- Admin users are no longer seeded with a hardcoded password.
-- Use APP_BOOTSTRAP_ADMIN_ENABLED=true and APP_BOOTSTRAP_ADMIN_PASSWORD=<strong secret>
-- for explicit one-time local bootstrap, then rotate or disable the bootstrap.

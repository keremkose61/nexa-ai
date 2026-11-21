-- ========================================================
-- NEXA AI Database Migration: V2__seed_data.sql
-- Description: Initial seed data for NEXA AI
-- Dialect: MySQL 8.0+
-- ========================================================

-- --------------------------------------------------------
-- 1. SEED ROLES
-- --------------------------------------------------------
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES 
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')), 'ROLE_USER', 'Standard user with default permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')), 'ROLE_ADMIN', 'System administrator with full privileges', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- --------------------------------------------------------
-- 2. SEED DEFAULT ADMIN USER
-- Credentials: admin@nexa.ai / Admin123!
-- BCrypt: $2a$10$.1tpIETc1CFp3hYHJZXv8u42F73HQmVRH3QWkUVsXNnKgMcE99IZO
-- --------------------------------------------------------
INSERT INTO users (id, email, password_hash, display_name, avatar_url, enabled, email_verified, last_login_at, created_at, updated_at)
VALUES 
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')), 
     'admin@nexa.ai', 
     '$2a$10$.1tpIETc1CFp3hYHJZXv8u42F73HQmVRH3QWkUVsXNnKgMcE99IZO', 
     'NEXA Administrator', 
     NULL, 
     TRUE, 
     TRUE, 
     NULL, 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE email = VALUES(email), password_hash = VALUES(password_hash);

-- --------------------------------------------------------
-- 3. ASSIGN ADMIN AND USER ROLES TO DEFAULT ADMIN
-- --------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
VALUES 
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')), UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', ''))),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')), UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')))
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- --------------------------------------------------------
-- 4. SEED DEFAULT USER SETTINGS FOR ADMIN
-- --------------------------------------------------------
INSERT INTO user_settings (
    id, 
    user_id, 
    theme, 
    language, 
    font_size, 
    default_provider_id, 
    default_model, 
    default_temperature, 
    default_max_tokens, 
    custom_instructions, 
    response_language, 
    conversation_retention_days, 
    notifications_enabled, 
    sidebar_collapsed, 
    created_at, 
    updated_at
)
VALUES (
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000020', '-', '')),
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')),
    'dark',
    'tr',
    'medium',
    NULL,
    'gpt-4o',
    0.7,
    2048,
    'You are NEXA AI, an enterprise desktop AI intelligence assistant.',
    'tr',
    90,
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

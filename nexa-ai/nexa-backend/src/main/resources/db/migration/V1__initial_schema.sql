-- ========================================================
-- NEXA AI Database Migration: V1__initial_schema.sql
-- Description: Complete MySQL 8 initial schema for NEXA AI
-- Dialect: MySQL 8.0+ / utf8mb4 / InnoDB
-- ========================================================

-- --------------------------------------------------------
-- 1. ROLES
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id BINARY(16) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 2. USERS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 3. USER_ROLES (Many-to-Many join table)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 4. USER_SETTINGS (One-to-One with User)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_settings (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    theme VARCHAR(20) DEFAULT 'dark',
    language VARCHAR(10) DEFAULT 'tr',
    font_size VARCHAR(20) DEFAULT 'medium',
    default_provider_id BINARY(16) NULL,
    default_model VARCHAR(100) NULL,
    default_temperature DOUBLE DEFAULT 0.7,
    default_max_tokens INT DEFAULT 2048,
    custom_instructions TEXT NULL,
    response_language VARCHAR(50) DEFAULT 'tr',
    conversation_retention_days INT DEFAULT 90,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sidebar_collapsed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_settings PRIMARY KEY (id),
    CONSTRAINT uk_user_settings_user_id UNIQUE (user_id),
    INDEX idx_user_settings_user_id (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 5. REFRESH_TOKENS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent VARCHAR(500) NULL,
    ip_address VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    INDEX idx_refresh_user_id (user_id),
    INDEX idx_refresh_token (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 6. AI_PROVIDERS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_providers (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    api_key_encrypted VARCHAR(1000) NULL,
    default_model VARCHAR(100) NULL,
    default_temperature DOUBLE DEFAULT 0.7,
    default_max_tokens INT DEFAULT 2048,
    timeout_seconds INT DEFAULT 60,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    last_tested_at TIMESTAMP NULL,
    last_test_success BOOLEAN NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_providers PRIMARY KEY (id),
    INDEX idx_provider_user_id (user_id),
    CONSTRAINT fk_ai_providers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 7. CONVERSATIONS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS conversations (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    title VARCHAR(500) NOT NULL DEFAULT 'New Conversation',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    ai_provider_id BINARY(16) NULL,
    ai_model VARCHAR(100) NULL,
    system_prompt TEXT NULL,
    total_messages INT NOT NULL DEFAULT 0,
    last_message_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_conversations PRIMARY KEY (id),
    INDEX idx_conv_user_id (user_id),
    INDEX idx_conv_updated_at (updated_at),
    INDEX idx_conv_ai_provider_id (ai_provider_id),
    CONSTRAINT fk_conversations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_provider FOREIGN KEY (ai_provider_id) REFERENCES ai_providers (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 8. MESSAGES
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS messages (
    id BINARY(16) NOT NULL,
    conversation_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content LONGTEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    ai_model VARCHAR(100) NULL,
    prompt_tokens INT NULL,
    completion_tokens INT NULL,
    total_tokens INT NULL,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    parent_message_id BINARY(16) NULL,
    error_message VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_messages PRIMARY KEY (id),
    INDEX idx_msg_conversation_id (conversation_id),
    INDEX idx_msg_created_at (created_at),
    INDEX idx_msg_parent_message_id (parent_message_id),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_parent FOREIGN KEY (parent_message_id) REFERENCES messages (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 9. AI_PROJECTS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_projects (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    system_instructions TEXT NULL,
    emoji VARCHAR(10) DEFAULT '🤖',
    color VARCHAR(20) DEFAULT '#7167F8',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_projects PRIMARY KEY (id),
    INDEX idx_project_user_id (user_id),
    CONSTRAINT fk_ai_projects_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 10. PROJECT_CONVERSATIONS (Many-to-Many join table)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS project_conversations (
    project_id BINARY(16) NOT NULL,
    conversation_id BINARY(16) NOT NULL,
    CONSTRAINT pk_project_conversations PRIMARY KEY (project_id, conversation_id),
    INDEX idx_proj_conv_project_id (project_id),
    INDEX idx_proj_conv_conversation_id (conversation_id),
    CONSTRAINT fk_proj_conv_project FOREIGN KEY (project_id) REFERENCES ai_projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_proj_conv_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 11. UPLOADED_FILES
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS uploaded_files (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    project_id BINARY(16) NULL,
    original_name VARCHAR(500) NOT NULL,
    stored_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(200) NULL,
    file_size BIGINT NULL,
    extracted_text LONGTEXT NULL,
    summary TEXT NULL,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processing_error VARCHAR(500) NULL,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_uploaded_files PRIMARY KEY (id),
    INDEX idx_file_user_id (user_id),
    INDEX idx_file_project_id (project_id),
    CONSTRAINT fk_uploaded_files_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_uploaded_files_project FOREIGN KEY (project_id) REFERENCES ai_projects (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 12. CUSTOM_ASSISTANTS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS custom_assistants (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    system_prompt TEXT NULL,
    emoji VARCHAR(10) DEFAULT '🤖',
    category VARCHAR(50) NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    use_count INT NOT NULL DEFAULT 0,
    default_model VARCHAR(100) NULL,
    default_temperature DOUBLE DEFAULT 0.7,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_custom_assistants PRIMARY KEY (id),
    INDEX idx_assistant_user_id (user_id),
    CONSTRAINT fk_custom_assistants_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 13. NOTIFICATIONS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url VARCHAR(500) NULL,
    reference_id BINARY(16) NULL,
    reference_type VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    INDEX idx_notif_user_id (user_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 14. AI_USAGE_LOGS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_usage_logs (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    conversation_id BINARY(16) NULL,
    message_id BINARY(16) NULL,
    provider_name VARCHAR(100) NULL,
    model_name VARCHAR(100) NULL,
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    request_duration_ms BIGINT NULL,
    feature_type VARCHAR(50) NULL,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_usage_logs PRIMARY KEY (id),
    INDEX idx_usage_user_id (user_id),
    INDEX idx_usage_created_at (created_at),
    CONSTRAINT fk_usage_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 15. AUDIT_LOGS
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NULL,
    resource_id BINARY(16) NULL,
    details TEXT NULL,
    ip_address VARCHAR(50) NULL,
    user_agent VARCHAR(500) NULL,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

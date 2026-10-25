-- Authentication and Authorization Schema

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    guest_session_token VARCHAR(255) UNIQUE,
    upload_count INTEGER NOT NULL DEFAULT 0,
    chat_message_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_guest_token ON users(guest_session_token);

CREATE TABLE IF NOT EXISTS linked_accounts (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_linked_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX IF NOT EXISTS idx_linked_accounts_user_id ON linked_accounts(user_id);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Link resumes table to users table
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS user_id VARCHAR(255);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_resumes_user'
    ) THEN
        ALTER TABLE resumes ADD CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON resumes(user_id);

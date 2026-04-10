-- Thêm các cột profile + admin management cho bảng users
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT NULL AFTER avatar_url;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at DATETIME NULL AFTER email_verified;
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE AFTER last_login_at;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_non_locked BOOLEAN DEFAULT TRUE AFTER enabled;

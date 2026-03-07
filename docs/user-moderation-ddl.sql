ALTER TABLE users
    ADD COLUMN moderation_strike INT NOT NULL DEFAULT 0,
    ADD COLUMN suspended_until DATETIME NULL,
    ADD COLUMN permanently_banned BIT(1) NOT NULL DEFAULT b'0';

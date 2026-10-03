-- =============================================================
-- Flyway Migration V8 - Create Password Reset Tokens Table
-- Database: ecommerce
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------
-- Table: password_reset_tokens
-- Description: Stores password reset tokens for forgot password flow
--              Follows BaseEntityAudit pattern with audit columns
-- Table Columns:
--   - id: Primary key (auto-increment)
--   - token_hash: SHA-256 hash of the raw token (unique, indexed)
--   - user_id: Foreign key to users table (indexed, cascade delete)
--   - expires_at: Token expiration timestamp (30 minutes from creation)
--   - used_at: Timestamp when token was used for password reset (nullable)
--   - created_date: Audit column for creation timestamp
--   - modified_date: Audit column for modification timestamp
--   - created_by: Audit column for creation user
--   - modified_by: Audit column for modification user
-- Lifecycle: Tokens are created on forgot-password request, marked
--            used when password reset succeeds, and cleaned up when
--            expired or used by the TokenCleanupScheduler
-- =============================================================
DROP TABLE IF EXISTS `password_reset_tokens`;
CREATE TABLE `password_reset_tokens` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `created_by`    varchar(255) DEFAULT NULL,
  `created_date`  datetime(6)  NOT NULL,
  `modified_by`   varchar(255) DEFAULT NULL,
  `modified_date` datetime(6)  DEFAULT NULL,
  `token_hash`    varchar(64)  NOT NULL,
  `user_id`       bigint       NOT NULL,
  `expires_at`    datetime(6)  NOT NULL,
  `used_at`       datetime(6)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_token_hash` (`token_hash`),
  KEY `FK_user_id` (`user_id`),
  KEY `IDX_expires_at` (`expires_at`),
  CONSTRAINT `FK_password_reset_tokens_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;


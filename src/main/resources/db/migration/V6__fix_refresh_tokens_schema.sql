-- =============================================================
-- Flyway Migration V6 - Fix refresh_tokens primary key
-- V5 used refresh_token as PK; align with BaseEntityAudit pattern
-- (auto-increment Long id + audit columns)
-- =============================================================

DROP TABLE IF EXISTS `refresh_tokens`;

CREATE TABLE `refresh_tokens` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `created_by`    varchar(255) DEFAULT NULL,
  `created_date`  datetime(6)  NOT NULL,
  `modified_by`   varchar(255) DEFAULT NULL,
  `modified_date` datetime(6)  DEFAULT NULL,
  `refresh_token` varchar(36)  NOT NULL,
  `user_id`       bigint       NOT NULL,
  `expires_at`    datetime(6)  NOT NULL,
  `revoked`       bit(1)       NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refresh_token` (`refresh_token`),
  KEY `idx_rt_user` (`user_id`),
  CONSTRAINT `fk_rt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

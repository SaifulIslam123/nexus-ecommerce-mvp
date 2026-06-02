-- =============================================================
-- Flyway Migration V4 - Rename USER role to SHOPPER, remove MODERATOR role
-- =============================================================

-- Step 1: Expand ENUM to include SHOPPER so the UPDATE below is accepted
ALTER TABLE `roles` MODIFY COLUMN `name` enum('ADMIN','COURIER','MODERATOR','USER','SHOPPER') DEFAULT NULL;

-- Step 2: Remove any user_roles assignments for MODERATOR before deleting the role
DELETE FROM `user_roles` WHERE `role_id` = (SELECT `id` FROM `roles` WHERE `name` = 'MODERATOR');

-- Step 3: Rename USER -> SHOPPER
UPDATE `roles` SET `name` = 'SHOPPER', `modified_date` = NOW(), `modified_by` = 'system' WHERE `name` = 'USER';

-- Step 4: Remove MODERATOR role record
DELETE FROM `roles` WHERE `name` = 'MODERATOR';

-- Step 5: Lock the ENUM down to only the current valid values
ALTER TABLE `roles` MODIFY COLUMN `name` enum('ADMIN','COURIER','SHOPPER') DEFAULT NULL;

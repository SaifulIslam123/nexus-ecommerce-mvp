-- 1. Add the foreign key column to the couriers table
ALTER TABLE couriers
ADD COLUMN user_id BIGINT; -- Assumes your User entity primary key type maps to BIGINT

-- 2. (Optional but recommended) Add a foreign key constraint to maintain data integrity
-- Assumes your users table primary key is named 'id'
ALTER TABLE couriers
ADD CONSTRAINT fk_couriers_user
FOREIGN KEY (user_id) REFERENCES users(id);

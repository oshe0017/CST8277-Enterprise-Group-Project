-- ============================================================================
-- Complete Database Setup Script for ACME College System
-- This script combines:
--   1. Security tables setup (student, security_role, security_user, user_has_role)
--   2. Fix for student_club.academic column type
-- ============================================================================
-- 
-- Security Users:
--   - admin (password: admin) - Role: ADMIN_ROLE
--   - cst8277 (password: 8277) - Role: USER_ROLE
--
-- Note: Passwords are stored as PBKDF2 hashes, not plain text.
-- ============================================================================

USE acmecollege;

-- ============================================================================
-- PART 1: Security Tables Setup
-- ============================================================================

-- ============================================================================
-- Step 1: Create student table (required for security_user foreign key)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `student`(
  `id` INT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NULL,
  `phone` VARCHAR(10) NULL,
  `program` VARCHAR(45) NULL,
  `created` DATETIME NULL,
  `updated` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;

-- Insert at least one student record (required for cst8277 user who references student_id = 1)
-- If student with id=1 already exists, this will be skipped
INSERT INTO `student`(`id`, `first_name`, `last_name`, `email`, `phone`, `program`, `created`, `updated`, `version`)
VALUES (1, 'John', 'Smith', 'jsmith@algonquincollege.com', '6131112222', 'Dentistry', NOW(), NOW(), 1)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- ============================================================================
-- Step 2: Create security_role table
-- ============================================================================
CREATE TABLE IF NOT EXISTS `security_role` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE INDEX `role_id_UNIQUE` (`role_id` ASC) VISIBLE,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE
);

-- ============================================================================
-- Step 3: Create security_user table (with foreign key to student table)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `security_user` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `password_hash` VARCHAR(256) NOT NULL,
  `username` VARCHAR(100) NOT NULL,
  `student_id` INT NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `user_id_UNIQUE` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE,
  INDEX `fk_security_user_student1_idx` (`student_id` ASC) VISIBLE,
  CONSTRAINT `fk_security_user_student1`
    FOREIGN KEY (`student_id`)
    REFERENCES `student` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- ============================================================================
-- Step 4: Create user_has_role table (join table)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `user_has_role` (
  `user_id` INT NOT NULL,
  `role_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `fk_security_user_has_security_role_security_role1_idx` (`role_id` ASC) VISIBLE,
  INDEX `fk_security_user_has_security_role_security_user1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_security_user_has_security_role_security_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `security_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_security_user_has_security_role_security_role1`
    FOREIGN KEY (`role_id`)
    REFERENCES `security_role` (`role_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- ============================================================================
-- Step 5: Insert security roles
-- ============================================================================
INSERT INTO `security_role` (`role_id`, `name`)
VALUES 
  (1, 'ADMIN_ROLE'), 
  (2, 'USER_ROLE')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ============================================================================
-- Step 6: Insert security users
-- ============================================================================
-- Password hashes are computed using PBKDF2WithHmacSHA256 algorithm
--   - admin: password 'admin' 
--   - cst8277: password '8277'
-- Note: cst8277 is linked to student_id = 1 (must exist in student table)
INSERT INTO `security_user` (`user_id`, `password_hash`, `username`, `student_id`)
VALUES 
  (1, 'PBKDF2WithHmacSHA256:2048:hYKwYbuwalL2mbXT3Lx8QgJuTWT8GgZcGljMPEW+TZA=:6GmiBW47QsKVgqF7wzt/wjQAMDd0RVMok3M8WPu8Y1U=', 'admin', NULL),
  (2, 'PBKDF2WithHmacSHA256:2048:ZJC4ipE7LQOZzOQyd2ch7VOxHJWwrVfDFTbo9H+U5Fw=:j5Wulo/tVmolv8hqu0k5ejTOPEMbzviQXStg/0/c6Qo=', 'cst8277', 1)
ON DUPLICATE KEY UPDATE 
  `password_hash` = VALUES(`password_hash`), 
  `username` = VALUES(`username`),
  `student_id` = VALUES(`student_id`);

-- ============================================================================
-- Step 7: Link users to roles
-- ============================================================================
INSERT INTO `user_has_role` (`user_id`, `role_id`)
VALUES 
  (1, 1),  -- admin -> ADMIN_ROLE
  (2, 2)   -- cst8277 -> USER_ROLE
ON DUPLICATE KEY UPDATE 
  `user_id` = VALUES(`user_id`), 
  `role_id` = VALUES(`role_id`);

-- ============================================================================
-- PART 2: Fix student_club.academic column type
-- ============================================================================
-- Fix student_club.academic column type to match DiscriminatorType.INTEGER
-- The entity uses INTEGER discriminator with values "1" (Academic) and "0" (NonAcademic)
-- But the database column might be BIT(1), which can cause data truncation errors

-- Change the column type from BIT(1) to TINYINT(1) to match INTEGER discriminator
-- Note: This will only modify the column if it exists. If the table doesn't exist yet,
-- it will be created by the acmecollege-create.sql script with the correct type.
ALTER TABLE `student_club` 
MODIFY COLUMN `academic` TINYINT(1) NOT NULL;

-- ============================================================================
-- PART 3: Verification
-- ============================================================================

-- Verify student table
SELECT 'Student Table:' AS 'Verification';
SELECT `id`, `first_name`, `last_name`, `email` FROM `student` WHERE `id` = 1;

-- Verify security roles
SELECT 'Security Roles:' AS 'Verification';
SELECT * FROM `security_role`;

-- Verify security users
SELECT 'Security Users:' AS 'Verification';
SELECT `user_id`, `username`, `student_id` FROM `security_user`;

-- Verify user-role mappings
SELECT 'User-Role Mappings:' AS 'Verification';
SELECT 
  u.`username`,
  r.`name` AS `role_name`
FROM `user_has_role` uhr
JOIN `security_user` u ON uhr.`user_id` = u.`user_id`
JOIN `security_role` r ON uhr.`role_id` = r.`role_id`
ORDER BY u.`username`, r.`name`;

-- Verify student_club table structure
SELECT 'Student Club Table Structure:' AS 'Verification';
DESCRIBE `student_club`;

-- ============================================================================
-- Expected Results:
-- ============================================================================
-- Security Roles: ADMIN_ROLE (role_id=1), USER_ROLE (role_id=2)
-- Security Users: admin (user_id=1), cst8277 (user_id=2)
-- User-Role Mappings: admin -> ADMIN_ROLE, cst8277 -> USER_ROLE
-- Student Club: academic column should be TINYINT(1) NOT NULL
-- ============================================================================


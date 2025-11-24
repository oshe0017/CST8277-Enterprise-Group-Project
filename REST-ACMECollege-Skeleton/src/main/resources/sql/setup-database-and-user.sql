-- ============================================================================
-- MySQL Database and User Setup Script
-- This script must be run FIRST as MySQL root user
-- ============================================================================
-- 
-- This script:
--   1. Creates the acmecollege database
--   2. Creates the cst8277 MySQL user
--   3. Grants all privileges on acmecollege database to cst8277 user
--
-- IMPORTANT: Run this script as root user BEFORE running other SQL scripts
-- ============================================================================

-- Create database (drop if exists for clean setup)
DROP DATABASE IF EXISTS acmecollege;
CREATE DATABASE acmecollege;

-- Create user (if not exists)
CREATE USER IF NOT EXISTS 'cst8277'@'localhost' IDENTIFIED BY '8277';

-- Grant all privileges on acmecollege database
GRANT ALL PRIVILEGES ON acmecollege.* TO 'cst8277'@'localhost';

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Verify user creation
SELECT 'MySQL User Verification:' AS 'Status';
SELECT User, Host FROM mysql.user WHERE User = 'cst8277';

-- Verify privileges
SELECT 'Privileges Verification:' AS 'Status';
SHOW GRANTS FOR 'cst8277'@'localhost';

-- ============================================================================
-- Expected Results:
-- ============================================================================
-- User: cst8277@localhost should exist
-- Grants: cst8277@localhost should have ALL PRIVILEGES on acmecollege.*
-- ============================================================================


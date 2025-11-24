@echo off
REM ============================================================================
REM Automated SQL Script Execution for ACMECollege Database Setup
REM ============================================================================
REM This script runs all SQL scripts in the correct order:
REM   1. setup-database-and-user.sql (as root)
REM   2. acmecollege-create.sql (as cst8277)
REM   3. acmecollege-data.sql (as cst8277)
REM ============================================================================

setlocal enabledelayedexpansion

REM Set the SQL scripts directory
set "SCRIPT_DIR=%~dp0src\main\resources\sql"
set "ROOT_DIR=%~dp0"

REM Check if we're in the right directory
if not exist "%SCRIPT_DIR%" (
    echo ERROR: SQL scripts directory not found!
    echo Expected: %SCRIPT_DIR%
    echo.
    echo Please run this script from the REST-ACMECollege-Skeleton directory.
    pause
    exit /b 1
)

REM ============================================================================
REM Find MySQL executable
REM ============================================================================
set "MYSQL_CMD=mysql"

REM First, try to find mysql in PATH
where mysql >nul 2>&1
if errorlevel 1 (
    REM MySQL not in PATH, try common installation locations
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
        set "MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    ) else if exist "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "MYSQL_CMD=C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else (
        echo.
        echo ERROR: MySQL executable not found!
        echo.
        echo Please either:
        echo   1. Add MySQL bin directory to your PATH, OR
        echo   2. Edit this script and set MYSQL_CMD to your MySQL path
        echo.
        echo Common locations:
        echo   C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
        echo   C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe
        echo.
        pause
        exit /b 1
    )
)

echo ============================================================================
echo ACMECollege Database Setup Script
echo ============================================================================
echo.
echo This script will:
echo   1. Create database and user (as MySQL root)
echo   2. Create all tables (as cst8277 user)
echo   3. Insert initial data (as cst8277 user)
echo.
echo ============================================================================
echo.

REM ============================================================================
REM Step 1: Setup Database and User (as root)
REM ============================================================================
echo [1/3] Setting up database and user...
echo.
echo NOTE: You will be prompted for MySQL root password.
echo.
"%MYSQL_CMD%" -u root -p < "%SCRIPT_DIR%\setup-database-and-user.sql"
if errorlevel 1 (
    echo.
    echo ERROR: Failed to run setup-database-and-user.sql
    echo Please check:
    echo   - MySQL is running
    echo   - Root password is correct
    echo   - You have permission to create databases
    pause
    exit /b 1
)
echo.
echo [SUCCESS] Database and user created successfully!
echo.
echo ============================================================================
echo.

REM ============================================================================
REM Step 2: Create Tables (as cst8277)
REM ============================================================================
echo [2/3] Creating database tables...
echo.
"%MYSQL_CMD%" -u cst8277 -p8277 acmecollege < "%SCRIPT_DIR%\acmecollege-create.sql"
if errorlevel 1 (
    echo.
    echo ERROR: Failed to run acmecollege-create.sql
    echo Please check:
    echo   - Database 'acmecollege' exists
    echo   - User 'cst8277' has proper privileges
    echo   - SQL script syntax is correct
    pause
    exit /b 1
)
echo.
echo [SUCCESS] Tables created successfully!
echo.
echo ============================================================================
echo.

REM ============================================================================
REM Step 3: Insert Initial Data (as cst8277)
REM ============================================================================
echo [3/3] Inserting initial data...
echo.
"%MYSQL_CMD%" -u cst8277 -p8277 acmecollege < "%SCRIPT_DIR%\acmecollege-data.sql"
if errorlevel 1 (
    echo.
    echo ERROR: Failed to run acmecollege-data.sql
    echo Please check:
    echo   - All tables exist
    echo   - SQL script syntax is correct
    echo   - Data doesn't violate constraints
    pause
    exit /b 1
)
echo.
echo [SUCCESS] Initial data inserted successfully!
echo.
echo ============================================================================
echo.

REM ============================================================================
REM Step 4: Verify Setup
REM ============================================================================
echo [VERIFY] Verifying database setup...
echo.
"%MYSQL_CMD%" -u cst8277 -p8277 acmecollege -e "SHOW TABLES;"
if errorlevel 1 (
    echo.
    echo WARNING: Could not verify tables. Please check manually.
) else (
    echo.
    echo [SUCCESS] Database verification complete!
)
echo.
echo ============================================================================
echo.

REM ============================================================================
REM Summary
REM ============================================================================
echo ============================================================================
echo SETUP COMPLETE!
echo ============================================================================
echo.
echo Database: acmecollege
echo User: cst8277
echo Password: 8277
echo.
echo Next steps:
echo   1. Rebuild the project: mvn clean install
echo   2. Deploy to Payara server
echo.
echo ============================================================================
echo.
pause


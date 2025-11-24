# REST-ACMECollege-Skeleton

## Backend REST API and Testing Documentation


## Database Setup

**IMPORTANT:** SQL scripts must be run **manually** before deploying the application. Hibernate is configured with `schema-generation.database.action=none`, so it will not automatically create tables.

### Quick Setup (Recommended)

Use the provided automation script:

**Windows Batch:**
```bash
run-sql-scripts.bat
```

The script will:
1. Prompt for MySQL root password
2. Create database and user
3. Create all tables
4. Insert initial data
5. Verify the setup

### Manual Setup

If you prefer to run scripts manually, execute them in this order:

1. **Create database and user** (as MySQL root):
   ```bash
   mysql -u root -p < src/main/resources/sql/setup-database-and-user.sql
   ```

2. **Create application tables** (as cst8277 user):
   ```bash
   mysql -u cst8277 -p8277 acmecollege < src/main/resources/sql/acmecollege-create.sql
   ```

3. **Populate initial data** (as cst8277 user):
   ```bash
   mysql -u cst8277 -p8277 acmecollege < src/main/resources/sql/acmecollege-data.sql
   ```

**Note:** The `complete-database.sql` script is not required when using the manual setup approach.

### Verify Database Setup

```sql
USE acmecollege;
SHOW TABLES;

-- Verify security tables
SELECT * FROM security_role;
SELECT * FROM security_user;
SELECT * FROM user_has_role;
```

**Expected Security Users:**
- `admin` (password: `admin`) - Role: `ADMIN_ROLE`
- `cst8277` (password: `8277`) - Role: `USER_ROLE`

---

## Building the Project

### Using Eclipse

1. **Right-click project** → **Maven** → **Update Project...**
2. **Right-click project** → **Run As** → **Maven build...**
3. Enter goals: `clean install`
4. Click **Run**

### Build Output

- **WAR file:** `target/REST-ACMECollege-Skeleton.war`

---

## Deployment

### Prerequisites

- **Database must be set up** (run SQL scripts first)
- **MySQL server must be running**
- **Payara server must be running**

### Deployment Steps

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Deploy to Payara:**
   - **Option A (Eclipse):** Right-click project → **Run As** → **Run on Server**
   - **Option B (Manual):** Deploy `target/REST-ACMECollege-Skeleton.war` via Payara Admin Console

3. **If deployment fails:**
   - Completely undeploy the old application
   - Clear Payara cache: Delete `glassfish/domains/domain1/generated/` folder
   - Restart Payara server
   - Redeploy

### Configuration Notes

- **Persistence Unit:** Configured with `schema-generation.database.action=none` (no automatic schema generation)
- **DataSource:** Uses property-based connection (databaseName, serverName, portNumber, user, password)
- **Database:** Must exist before deployment (created via SQL scripts)

### Verify Deployment

1. **Check Admin Console:** `http://localhost:4848` → **Applications**
   - Application should show as "Enabled"
   - Check for any deployment errors

2. **Test endpoint:** `http://localhost:8080/REST-ACMECollege-Skeleton/api/v1/student`
   - Should prompt for Basic Auth
   - Use: `admin` / `admin`
   - Should return JSON list of students

---

## Running Tests

### Prerequisites

- **Payara Server must be running** with the application deployed
- **Database must be set up** with security tables populated
- **Application must be accessible** at `http://localhost:8080/REST-ACMECollege-Skeleton`

### Run Tests via Maven

```bash
# Run all tests
mvn test

# Run with test report
mvn test surefire-report:report
```

### Test Report

After running tests, view the report:
- **Location:** `target/site/surefire-report.html`
- **JUnit Tests:** 24 tests covering CRUD operations, security roles, and negative scenarios
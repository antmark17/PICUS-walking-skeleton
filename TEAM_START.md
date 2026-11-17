# PICUS — Team Setup and Quick Start

This document explains how to set up, run and test the integrated PICUS Walking Skeleton.

The repository contains the contributions of all three development groups:

- G1 — Identity & Organizations
- G2 — Requests
- G3 — Frontend

The current target is a single integrated Spring Boot application.

## 1. Requirements

Install or verify:

- Java JDK 21
- Git
- An IDE (IntelliJ IDEA or VS Code recommended)

Maven is provided through the Maven Wrapper.

Check the Java installation:

```bash
java -version
```

Make sure Java 21 is active.

### Windows: check JAVA_HOME

PowerShell:

```powershell
echo $env:JAVA_HOME
Test-Path "$env:JAVA_HOME\bin\java.exe"
```

If necessary, configure `JAVA_HOME` to point to the JDK 21 installation directory.

## 2. Clone the Repository

```bash
git clone https://github.com/antmark17/PICUS-walking-skeleton.git
cd PICUS-walking-skeleton
```

Use the integrated `main` branch:

```bash
git switch main
git pull origin main
```

Check the current branch:

```bash
git branch --show-current
```

Expected result:

```text
main
```

## 3. Configure Demo Credentials

PICUS provides three synthetic users:

| Username | Role | Organization |
|---|---|---|
| `banco` | BANCO | No organization |
| `opt_a` | OPT | OPT A |
| `opt_b` | OPT | OPT B |

All demo users share the same password.

The password is read from the `PICUS_DEMO_PASSWORD` environment variable.

### Windows PowerShell

```powershell
$env:PICUS_DEMO_PASSWORD = "PicusDemo2026!"
```

### macOS / Linux

```bash
export PICUS_DEMO_PASSWORD="PicusDemo2026!"
```

The variable must be configured in the terminal session used to start the application.

If it is not configured, a random password is generated at startup and the documented demo credentials will not be usable.

Never commit passwords or real credentials to Git.

## 4. Verify the Project

Before starting the application, run the complete Maven verification.

### Windows

```powershell
.\mvnw.cmd clean verify
```

### macOS / Linux

```bash
./mvnw clean verify
```

Expected result:

```text
BUILD SUCCESS
```

The test suite includes security, application, persistence, MVC and end-to-end tests.

## 5. Start PICUS

Keep the same terminal session in which the demo password was configured.

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Wait until Spring Boot reports that the application has started.

Do not close the terminal while using the application.

Open the login page:

http://localhost:8080/login

To stop the application, press:

```text
CTRL + C
```

## 6. Application Walkthrough

Use the following sequence to verify the integrated functionality.

### Step 1 — Login as OPT A

Credentials:

```text
Username: opt_a
Password: your configured PICUS_DEMO_PASSWORD
```

Expected behavior:

- Successful login.
- Redirect to `/opt/requests`.
- Access to the OPT request dashboard.

### Step 2 — Create a request

1. Open the new request page.
2. Select one of the available development measures.
3. Submit the form.
4. Verify that the application redirects to the request detail page.

The initial request status is:

```text
DA_VALUTARE
```

The request organization is derived from the authenticated user.

### Step 3 — Login as BANCO

Log out and authenticate using:

```text
Username: banco
Password: your configured PICUS_DEMO_PASSWORD
```

Expected behavior:

- Redirect to `/banco/requests`.
- Access to the requests submitted by OPT organizations.
- Ability to open request details.

### Step 4 — Start processing

1. Open the request created by OPT A.
2. Start processing.
3. Verify that the request status changes to:

```text
IN_ELABORAZIONE
```

### Step 5 — Verify OPT A

Log out and authenticate again as:

```text
Username: opt_a
```

Open the same request.

The updated status should now be visible:

```text
IN_ELABORAZIONE
```

### Step 6 — Verify organization isolation

Log out and authenticate as:

```text
Username: opt_b
```

OPT B must not be able to see or access the request created by OPT A.

This verifies the ownership and authorization rules implemented in the backend.

## 7. Main Application Routes

| Route | Description | Role |
|---|---|---|
| `/login` | Login page | Public |
| `/opt/requests` | OPT dashboard | OPT |
| `/opt/requests/new` | Create request | OPT |
| `/opt/requests/{id}` | Request details | OPT owner |
| `/banco/requests` | BANCO dashboard | BANCO |
| `/banco/requests/{id}` | Request details | BANCO |
| `/banco/requests/{id}/start` | Start processing (POST) | BANCO |
| `/health` | Application health endpoint | Authenticated |

## 8. Database and Local Development

The application uses H2 and Spring Data JPA.

Local development database:

```text
picus-dev.mv.db
```

The database is file-based, so application data can persist between restarts.

Tests use a separate in-memory H2 database.

Do not commit local database files or real user information.

## 9. Git Collaboration Workflow

Always start new work from the latest `main`.

```bash
git switch main
git pull origin main
git switch -c feature/your-feature-name
```

After implementing and testing your changes:

```bash
git add .
git commit -m "feat: describe your changes"
git push -u origin feature/your-feature-name
```

Then open a Pull Request targeting `main`.

Before merging:

- Review the changed files.
- Run `clean verify`.
- Check the GitHub Actions result.
- Obtain the required review approval.

Do not push directly to `main` for ordinary feature development.

## 10. Troubleshooting

### JAVA_HOME is not defined correctly

Make sure `JAVA_HOME` points to the root of a JDK 21 installation, not its `bin` directory.

### Port 8080 is already in use

Stop the other application using port 8080 before starting PICUS.

### Login fails

Check that `PICUS_DEMO_PASSWORD` was configured in the same terminal session used to start Spring Boot.

Remember that all three demo accounts share the configured password.

If the application was started without the variable, restart it with the variable configured.

### Maven tests fail

Run:

```bash
./mvnw clean verify
```

On Windows:

```powershell
.\mvnw.cmd clean verify
```

Inspect the first failing test and its error message before modifying application code.

---

## Project Responsibilities

| Group | Area | Main responsibility |
|---|---|---|
| G1 | Identity & Organizations | Authentication, roles, authorization and organization identification |
| G2 | Requests | Domain logic, request lifecycle and persistence |
| G3 | Frontend | Thymeleaf interfaces, controllers and web integration |

The groups share a single modular Spring Boot application.

Changes to shared contracts should be discussed before implementation.

---

**Repository:** https://github.com/antmark17/PICUS-walking-skeleton

**Current branch for development:** `main`

**Project status:** Integrated Walking Skeleton — G1 + G2 + G3
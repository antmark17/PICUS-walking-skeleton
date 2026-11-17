# PICUS — Walking Skeleton

## Project Overview

PICUS is a web application developed for Banco Alimentare Campania as part of a university Software Architecture and Design project.

The Walking Skeleton integrates the work of three development groups into a single Spring Boot application:

- **G1 — Identity & Organizations:** authentication, authorization, user roles and organization identification.
- **G2 — Requests:** request management, business rules, state transitions and database persistence.
- **G3 — Frontend:** web interfaces, navigation, forms and integration with the backend.

The current implementation provides an integrated and executable foundation for the PICUS application. It represents the Walking Skeleton, not the final production-ready MVP.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Security | Spring Security |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Database | H2 |
| Persistence | Spring Data JPA |
| Build system | Maven |
| Testing | JUnit 5, Spring Boot Test, MockMvc |
| Version control | Git and GitHub |

## Architecture

PICUS follows a modular monolithic architecture.

The main application modules are:

### Identity and Organizations — G1

Responsible for:
- Authentication through Spring Security.
- Login and logout.
- Role-based authorization.
- Identification of the authenticated actor.
- Organization identification for OPT users.
- Separation between BANCO and OPT permissions.

Supported roles:
- `BANCO`
- `OPT`

The organization associated with an OPT user is derived from the authenticated principal, never from browser-submitted data.

### Requests — G2

Responsible for:
- Request creation.
- Request ownership.
- Request retrieval.
- Business rules and state transitions.
- Persistence through JPA and H2.

The main application service is `RequestService`, which implements the `RequestUseCases` contract.

The current request states are:

- `DA_VALUTARE`
- `IN_ELABORAZIONE`

The transition to `IN_ELABORAZIONE` is initiated by a BANCO user.

### Web and Frontend — G3

Responsible for:
- Thymeleaf pages.
- Login interface.
- OPT request dashboard.
- OPT request creation form.
- OPT request details.
- BANCO request dashboard.
- BANCO request details.
- Integration with backend use cases.

The frontend communicates with the application through Spring MVC controllers and does not access JPA repositories directly.

## Main Application Flows

### OPT workflow

1. The OPT user logs in.
2. The application redirects the user to `/opt/requests`.
3. The user can view requests belonging to their organization.
4. The user can create a new request.
5. The application associates the request with the authenticated OPT organization.
6. The user can view the request details and its current status.

### BANCO workflow

1. The BANCO user logs in.
2. The application redirects the user to `/banco/requests`.
3. The user can view all submitted requests.
4. The user can open a request detail page.
5. The user can start processing a request.
6. The request status changes from `DA_VALUTARE` to `IN_ELABORAZIONE`.

### Security and ownership

- OPT users can access only requests belonging to their organization.
- BANCO users can access the BANCO request area.
- Authorization is enforced on the backend.
- CSRF protection remains enabled for POST forms.
- Request ownership is derived server-side from the authenticated principal.

## Main HTTP Endpoints

| Method | Endpoint | Access |
|---|---|---|
| GET | `/login` | Public |
| POST | `/login` | Public, Spring Security |
| POST | `/logout` | Authenticated |
| GET | `/opt/requests` | OPT |
| GET | `/opt/requests/new` | OPT |
| POST | `/opt/requests` | OPT |
| GET | `/opt/requests/{id}` | OPT, owner only |
| GET | `/banco/requests` | BANCO |
| GET | `/banco/requests/{id}` | BANCO |
| POST | `/banco/requests/{id}/start` | BANCO |
| GET | `/health` | Authenticated |

## Demo Accounts

The Walking Skeleton includes three synthetic accounts:

| Role | Username | Organization |
|---|---|---|
| BANCO | `banco` | None |
| OPT A | `opt_a` | OPT A |
| OPT B | `opt_b` | OPT B |

All three accounts use the same password, configured through the `PICUS_DEMO_PASSWORD` environment variable.

Passwords are encoded using BCrypt and are not stored in the repository.

If the environment variable is not configured, the application generates a random password at startup. Therefore, configure the variable before logging in.

These accounts are intended exclusively for local development and testing.

## Prerequisites

- JDK 21
- Git
- Windows PowerShell, macOS Terminal or Linux shell

The repository includes the Maven Wrapper. Maven does not need to be installed separately.

Verify Java:

```bash
java -version
```

The installed version must be Java 21.

## Getting Started

Clone the repository:

```bash
git clone https://github.com/antmark17/PICUS-walking-skeleton.git
cd PICUS-walking-skeleton
```

Update the local repository:

```bash
git switch main
git pull origin main
```

### Configure the demo password

Windows PowerShell:

```powershell
$env:PICUS_DEMO_PASSWORD = "PicusDemo2026!"
```

macOS / Linux:

```bash
export PICUS_DEMO_PASSWORD="PicusDemo2026!"
```

Use a temporary password exclusively for local development. Do not commit credentials.

### Run tests

Windows:

```powershell
.\mvnw.cmd clean verify
```

macOS / Linux:

```bash
./mvnw clean verify
```

### Start the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Open the application:

http://localhost:8080/login

## Database

The application uses H2 with Spring Data JPA.

Local development configuration:

- Database type: H2
- Storage: file-based
- Database file: `picus-dev.mv.db`
- Hibernate schema management: `update`

The test environment uses a separate in-memory H2 database with `create-drop`.

The local database file is development data and must not be committed to Git.

## Testing

The project includes unit, integration, security and web tests.

The integrated end-to-end tests cover:

- Login for BANCO, OPT A and OPT B.
- OPT request creation.
- Request ownership assignment.
- BANCO request consultation.
- Starting request processing.
- Updated status visibility.
- OPT organization isolation.
- Authorization between roles.
- CSRF protection.
- Form validation.

The integrated branch was previously verified with:

```text
Tests run: 94
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Run `clean verify` again after every relevant change to verify the current version.

## Continuous Integration

GitHub Actions is configured to automate Maven verification.

The CI workflow is intended to:
- Check out the repository.
- Set up Java 21.
- Run Maven verification.
- Report the build result on GitHub.

Pull Requests targeting `main` should be checked before merging.

## Project Scope and Limitations

The current implementation is a Walking Skeleton.

The following elements are still outside the current implementation or require further development:

- Production authentication and user management.
- Official and dynamic measure catalog.
- Real organization registry.
- Production database configuration.
- Advanced request lifecycle and additional statuses.
- Appointment management.
- Operator and service-provider management.
- Notifications and reporting.
- Production deployment and operational configuration.

The available measures (`Misura A`, `Misura B`, `Misura C`) are temporary development values and do not represent the final official catalog.

No personal identifying information about beneficiaries should be introduced into test data.

## Development Workflow

The main branch contains the integrated implementation.

For future development:

1. Pull the latest `main`.
2. Create a dedicated feature branch.
3. Implement the assigned functionality.
4. Run `clean verify`.
5. Push the branch.
6. Open a Pull Request towards `main`.
7. Review and merge only after the checks pass.

Avoid modifying shared API contracts without agreement between the responsible groups.

---

**Repository:** https://github.com/antmark17/PICUS-walking-skeleton

**Project:** PICUS — Banco Alimentare Campania

**Status:** Integrated Walking Skeleton — G1 + G2 + G3
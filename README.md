# Digital Library Management System API

Spring Boot REST API for library reservations. Patrons browse/reserve books, librarians checkout/return them.

[Source code](src/main/java/com/library) | [Tests](src/test/java/com/library)

## Quickstart

```bash
# Build
./mvnw clean install

# Run (dev profile, H2 in-memory)
./mvnw spring-boot:run

# Test
./mvnw test
```

App runs at `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

## Design Decisions

Upgraded to Spring Boot 4.1.1 (spec called for 3.2+), since 3.5.x reached end-of-life in June 2026.

`borrowingHistory` on the profile endpoint counts completed (`RETURNED`) reservations, per api-contracts.md's wording.

Reservation creation and return use a pessimistic row lock on the book being modified, closing a last-copy race condition under concurrent requests.

## Starter Code Issues Found & Fixed

- `application.properties` defaulted the active profile to `prod`, not `dev`. Fixed.
- Identical hardcoded JWT secret shared between dev and prod configs. Prod now requires `JWT_SECRET` from the environment with no fallback.
- Leftover copy-paste naming (`com.example.demo`, `studentdb`). Renamed throughout.
- Actuator health endpoint exposed full details on a public route in prod. Restricted to status only.

## Deployment

Not deployed. Course AWS sandbox blocks RDS creation and permission changes at the IAM level. Raised with course staff. App runs and passes all tests locally.
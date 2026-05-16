# TBR — Books, Movies, Music

A small Spring Boot full-stack demo built for ITP246. Tracks three categories of culture & entertainment list items with a homepage overview and three themed subpages: a library (books), a cinema (movies), and a disco club (music).

**Live at:** https://tbr.benlamb.net — continuously deployed on every push to `main`.

---

## What's in the box

- **Spring Boot 4.0.1** on **Java 25** (Temurin)
- **Thymeleaf** server-side rendering (per the assignment's Thymeleaf-or-REST requirement)
- **Spring Data JPA** with two database profiles:
  - **Local dev:** H2 in-memory (resets on restart, sample data via `data.sql`)
  - **Production:** PostgreSQL via Neon (free tier, persists across VPS restarts)
- **JUnit 5 / MockMvc** tests
- **CDS-accelerated boot** (~3s cold start) via a per-deploy CDS archive
- Themed error page that replaces Spring's Whitelabel default

## Run it locally

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080`. The H2 console is at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tbrdb`, user `sa`, no password). Sample data preloads from `src/main/resources/data.sql`.

## Run the tests

```bash
mvn test
```

## Deploy

Production runs as a fat JAR managed by `systemd` on a single VPS, fronted by nginx with a Let's Encrypt cert. The deploy pipeline lives in [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) and fires on every push to `main`:

1. `mvn test` — fail fast on a broken build
2. `mvn clean package` — produce the fat JAR
3. SCP the JAR to `/opt/tbr/incoming/` on the VPS
4. Atomic swap (`mv app.jar app.jar.prev; mv incoming/app.jar app.jar`) — one-line rollback by reversing the rename
5. Regenerate the CDS archive against the new JAR (~3s cold-start budget)
6. `systemctl restart tbr` and verify the unit is active

See [`deploy/README.md`](deploy/README.md) for the one-time VPS setup (service user, systemd unit, sudoers, nginx vhost) and day-to-day ops (status, logs, rollback).

### Database

A free Neon Postgres instance backs prod. The JDBC URL lives in `/opt/tbr/.env` and is read by the systemd unit, so the JAR itself never sees credentials at build time. The `prod` profile (`SPRING_PROFILES_ACTIVE=prod`) is activated in the same env file.

## Project layout

```
tbr-app/
├── pom.xml
├── .github/workflows/deploy.yml        # build → SCP → swap → restart on push to main
├── deploy/                             # VPS deployment artifacts (systemd unit, sudoers, .env template, ops README)
├── src/main/java/com/blamb/tbr/
│   ├── TbrApplication.java
│   ├── model/                          # Category enum + TbrItem entity
│   ├── repository/                     # Spring Data JPA repo
│   ├── service/                        # Service layer
│   └── controller/                     # MVC controller (home + 3 subpages + CRUD)
├── src/main/resources/
│   ├── application.properties          # H2 in-memory (dev default)
│   ├── application-prod.properties     # Postgres (VPS + Neon)
│   ├── data.sql                        # Sample seed data
│   ├── static/css/                     # common + 3 themes
│   └── templates/                      # Thymeleaf views (incl. error.html)
└── src/test/java/com/blamb/tbr/
    └── TbrApplicationTests.java
```

## Why H2 in dev, Postgres in prod

H2 in-memory keeps local development fast and zero-setup: anyone with the repo can `mvn spring-boot:run` and have a working app in 30 seconds with sample data. But in-memory means every restart is a fresh database, which is fine for local iteration and bad for a deployed demo where the grader expects their additions to stick. The production profile (`SPRING_PROFILES_ACTIVE=prod`) swaps to Postgres via env vars for the live online database, with no code changes required. Same JPA entities, same repository, same service layer. Spring **profiles** handle the dialect and driver swap.

## License

VWCC ITP246 2026 Coursework, not for redistribution.

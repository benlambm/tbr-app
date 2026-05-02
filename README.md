# TBR — Books, Movies, Music

A small Spring Boot full-stack demo built for ITP246. Tracks three categories of "to be read (tbr) / watched / heard" items with a homepage overview and three themed subpages: a library (books), a cinema (movies), and a disco club (music).

**Live continuous deployment triggered with any GitHub update to:** https://tbr-app.onrender.com

---

## What's in the box

- **Spring Boot 3.4** with Java 21
- **Thymeleaf** server-side rendering (per the assignment's Thymeleaf-or-REST requirement)
- **Spring Data JPA** with two database profiles:
  - **Local dev:** H2 in-memory (resets on restart, sample data via `data.sql`)
  - **Production:** PostgreSQL via Neon (free tier, persists across Render's free restart cycles)
- **JUnit 5 / MockMvc** tests 

## Run it locally

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080`. The H2 console is at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tbrdb`, user `sa`, no password). Sample data preloads from `src/main/resources/data.sql`.

## Run the tests

```bash
mvn test
```

## Deploy to Render with persistent Postgres

The free Render tier has an ephemeral filesystem, so any local file-based DB is wiped on every redeploy and likely on every cold-start spin-down. To survive sleep cycles, the production profile points at an external online database (Neon).

### 1. Provision a free Neon database

1. Sign up at [neon.tech](https://neon.tech) (GitHub auth works).
2. Create a project and a database. Default `neondb` is fine.
3. From the connection details, grab the Postgres connection string. It looks like:
   ```
   postgresql://USER:PASSWORD@ep-xxxx-xxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```
4. **Convert it to JDBC format** by prepending `jdbc:` and moving credentials into query params:
   ```
   jdbc:postgresql://ep-xxxx-xxxx.us-east-2.aws.neon.tech/neondb?sslmode=require&user=USER&password=PASSWORD
   ```

### 2. Push this repo to GitHub

```bash
git init
git add .
git commit -m "Initial TBR app"
git remote add origin https://github.com/YOUR_USERNAME/tbr-app.git
git push -u origin main
```

### 3. Create a Render web service

1. Go to [render.com](https://render.com) and sign up (GitHub auth, no credit card).
2. **New → Web Service →** connect your `tbr-app` repo.
3. Settings:
   - **Runtime:** Docker (Render auto-detects the `Dockerfile`)
   - **Instance type:** Free
   - **Region:** (whichever is closer)
4. **Environment variables:**

   | Key                       | Value |
   |---------------------------|-------|
   | `SPRING_PROFILES_ACTIVE`  | `prod` |
   | `JDBC_DATABASE_URL`       | the JDBC URL from Neon (full string with credentials) |

5. Click **Create Web Service.** First build takes ~5–8 minutes (Maven downloads dependencies). Subsequent builds are faster due to Docker layer caching.

### 4. Verify

Render gives you a URL like `https://tbr-app.onrender.com`. Open it. Add a book, a movie, a song. Reload—your data is still there because your database is cloud-native on Neon.

**Cold-start note:** the first request after sleep takes 30–60 seconds. Render shows a loading page while the JAR boots.

## Project layout

```
tbr-app/
├── pom.xml
├── Dockerfile                          # 5-line multi-stage build for Render
├── src/main/java/com/blamb/tbr/
│   ├── TbrApplication.java
│   ├── model/                          # Category enum + TbrItem entity
│   ├── repository/                     # Spring Data JPA repo
│   ├── service/                        # Service layer
│   └── controller/                     # MVC controller (home + 3 subpages + CRUD)
├── src/main/resources/
│   ├── application.properties          # H2 in-memory (dev default)
│   ├── application-prod.properties     # Postgres (Render/Neon)
│   ├── data.sql                        # Sample seed data
│   ├── static/css/                     # common + 3 themes
│   └── templates/                      # Thymeleaf views
└── src/test/java/com/blamb/tbr/
    └── TbrApplicationTests.java
```

## Why H2 in dev, Postgres in prod

H2 in-memory keeps local development fast and zero-setup: anyone with the repo can `mvn spring-boot:run` and have a working app in 30 seconds with sample data. But in-memory means every restart is a fresh database, which is fine for local iteration and bad for a deployed demo where the grader expects their additions to stick. The production profile (`SPRING_PROFILES_ACTIVE=prod`) swaps to postgres via env vars for the live online database, with no code changes required. Same JPA entities, same repository, same service layer. Spring **profiles** handle the dialect and driver swap.

## License

VWCC ITP246 2026 Coursework, not for redistribution.

# Spring Boot → Render Deployment SOP

**ITP246 — May 2026 edition.** A step-by-step playbook to get your Spring Boot final project live on the public internet with **persistent data**, using only free services and no credit card.

**Total time:** ~25 minutes (including the first Render build).
**Prerequisites:** A working Spring Boot app locally (`mvn spring-boot:run` succeeds, `mvn test` passes), a GitHub account, and `git` installed.
**Reference implementation:** https://github.com/benlambm/tbr-app — [live demo](https://tbr-app.onrender.com).

---

## Stack at a glance

| Layer | Tool | Why |
|---|---|---|
| Code hosting | **GitHub** | Free, integrates with Render's auto-deploy |
| Build & host | **Render** (Docker, Free tier) | No credit card, auto-deploy from `main` |
| Production DB | **Neon Postgres** (Free tier) | 0.5GB, no expiration, no credit card |
| Local dev DB | **H2 in-memory** | Zero setup, fast restarts, satisfies the assignment requirement |
| Environment switch | `SPRING_PROFILES_ACTIVE=prod` | Same code, different DB per environment |

> **Why not just use H2 in production?** Render's free tier has an **ephemeral filesystem** — the container is destroyed and recreated on every redeploy and every cold-start spin-up. A file-based H2 database on that filesystem gets wiped. Postgres lives in Neon (not in Render's container), so your data survives. Spring profiles let you keep H2 locally and Postgres in production with zero code changes.

---

## Phase 0 — Make your repo deploy-ready

Before deploying, your repo needs four things in addition to your normal Spring Boot code. **Copy these templates exactly** to avoid the common gotchas.

### `pom.xml` — both DB drivers must be present

```xml
<!-- H2 for local dev -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<!-- Postgres for production -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### `src/main/resources/application.properties` — local dev (H2)

```properties
# H2 in-memory: resets on every restart, sample data loads from data.sql
spring.datasource.url=jdbc:h2:mem:appdb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.defer-datasource-initialization=true

spring.sql.init.mode=always
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.thymeleaf.cache=false

server.port=${PORT:8080}
```

### `src/main/resources/application-prod.properties` — production (Postgres)

```properties
# Activated by SPRING_PROFILES_ACTIVE=prod env var on Render
spring.datasource.url=${JDBC_DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

spring.sql.init.mode=never
spring.h2.console.enabled=false
spring.thymeleaf.cache=true

server.port=${PORT:8080}
```

### `Dockerfile` — copy this **exactly**

```dockerfile
# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

> ⚠️ **Use `eclipse-temurin`, not `openjdk`.** The `openjdk` Docker image was deprecated in 2022. Eclipse Temurin is the maintained successor.
>
> ⚠️ **Both paths must be absolute** (`/app/app.jar`). Mixing relative and absolute paths on the COPY and ENTRYPOINT lines will build successfully and then fail at runtime with `Error: Unable to access jarfile`.

### `.gitignore` — exclude build output

Make sure `target/` and IDE files are excluded so you don't push 60+ MB of compiled classes. The default Spring Initializr `.gitignore` already covers this.

---

## Phase 1 — Push to GitHub (5 min)

From your project root in a terminal:

```bash
git init -b main
git add .
git commit -m "Initial commit"
```

**Option A — GitHub CLI** (one command):

```bash
gh repo create your-app-name --public --source=. --push
```

**Option B — GitHub web UI:**

1. Go to https://github.com/new
2. Create a **public** repo named `your-app-name`. Do NOT add a README or .gitignore (those exist locally).
3. Copy the URL GitHub gives you, then:
   ```bash
   git remote add origin <URL>
   git push -u origin main
   ```

✅ **Verify:** Visit `https://github.com/YOUR_USERNAME/your-app-name`. You should see your code.

---

## Phase 2 — Provision Neon Postgres (3 min)

1. Go to https://console.neon.tech and **Sign up with GitHub.** No credit card.
2. Create a project. Defaults are fine.
3. The dashboard shows a **Connection String** like:
   ```
   postgresql://neondb_owner:npg_ABC123@ep-foo.us-east-1.aws.neon.tech/neondb?sslmode=require
   ```
4. **Convert to JDBC format.** This is the trickiest step — read it twice:

   **Postgres URL format:**
   ```
   postgresql://USER:PASSWORD@HOST/DB?sslmode=require
   ```

   **JDBC format:**
   ```
   jdbc:postgresql://HOST/DB?sslmode=require&user=USER&password=PASSWORD
   ```

   Two changes:
   - Prepend `jdbc:`
   - Move `USER:PASSWORD@` out of the host and into the query string as `&user=USER&password=PASSWORD`

   **Worked example:**
   ```
   FROM:  postgresql://neondb_owner:npg_ABC123@ep-foo.us-east-1.aws.neon.tech/neondb?sslmode=require
   TO:    jdbc:postgresql://ep-foo.us-east-1.aws.neon.tech/neondb?sslmode=require&user=neondb_owner&password=npg_ABC123
   ```

> ⚠️ **Use the direct host, not the "pooler" host.** Neon shows two hostnames; pick the one **without** `-pooler` in it. The pooler (PgBouncer) interferes with Hibernate's prepared-statement caching during first-boot schema creation.

✅ **Verify:** Save the JDBC URL in a password manager or a scratch note. **Do NOT commit it to git.** You'll paste it into Render next.

---

## Phase 3 — Deploy to Render (10 min)

1. Go to https://dashboard.render.com → **Sign up with GitHub.** Authorize Render to read your repos.
2. Click **New + → Web Service.** Pick your repo from the list.
3. Configuration:

   | Field | Value |
   |---|---|
   | Name | `your-app-name` (becomes `your-app-name.onrender.com`) |
   | Region | **Ohio** (closest to Neon's `us-east-1`) |
   | Branch | `main` |
   | Runtime | **Docker** (auto-detected from your Dockerfile) |
   | Instance Type | **Free** |

4. Scroll down to **Environment Variables.** Add exactly two:

   | Key | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `JDBC_DATABASE_URL` | *(your JDBC URL from Phase 2)* |

5. Click **Advanced.** ⚠️ **Critical:** find **Health Check Path** and **clear the field.** Render pre-fills it with `/healthz`, which your app does not have. If you leave it, Render will return 404, mark your service unhealthy, and restart it in a loop.

6. Leave everything else at default. Click **Create Web Service.**

7. Watch the build logs. **First build is 5–8 minutes** (Maven downloads dependencies into the Docker layer cache); subsequent builds are ~3 minutes thanks to caching.

✅ **Verify:** When the logs show `Started YourApplication in X.XXX seconds`, open `https://your-app-name.onrender.com` in a browser. Your home page should load.

---

## Phase 4 — Test persistence

This is the whole point of using external Postgres:

1. Add a real piece of data through your app's UI (a recipe, a task, whatever your app does).
2. Wait **16+ minutes** (Render's free tier sleeps after 15 minutes of inactivity).
3. Reload the page. The first request takes **30–60 seconds** while the container wakes up — Render shows a loading screen. After that, your data is still there.

The data lives in Neon, not in Render. When Render destroys and recreates the container, Neon's data survives.

✅ **Verify:** After cold-start wakeup, your test data is still visible.

---

## Phase 5 — Iteration (CI/CD)

You are now on a Continuous Deployment pipeline. To ship changes:

```bash
# Make changes locally, test
mvn test

# Commit and push
git add .
git commit -m "Describe your change"
git push origin main
```

Render detects the push within ~30 seconds and starts a new build automatically. **No manual deploy step.** This is what "Continuous Deployment" means in practice.

---

## Common errors and fixes

| Symptom | Cause | Fix |
|---|---|---|
| `Error: Unable to access jarfile /app.jar` | Dockerfile path mismatch | Use the Dockerfile in Phase 0 verbatim. Both paths must be absolute (`/app/app.jar`). |
| Build succeeds but service is "Unhealthy," restarts forever | Health Check Path is `/healthz` — your app returns 404 | Render dashboard → Settings → Health Check Path → clear the field → Save. |
| `FATAL: password authentication failed for user "neondb_owner"` | Malformed JDBC URL | Re-do the Phase 2 conversion. Credentials live in the query string (`&user=...&password=...`), not in `user:pass@host` form. |
| `Driver org.h2.Driver claims to not accept jdbcUrl jdbc:postgresql://...` | `SPRING_PROFILES_ACTIVE` not set — app loaded H2 config | Render dashboard → Environment → confirm `SPRING_PROFILES_ACTIVE=prod` is present. Save (triggers redeploy). |
| Data resets every redeploy | App is writing to H2 file on Render's ephemeral disk | Use external Postgres (Neon). Render's free-tier filesystem is wiped on every restart. |
| First request after sleep takes 30–60 seconds | Cold start (Render free tier) | Expected. Mention it in any live demo. Pre-warm by visiting the URL ~2 minutes before showing it. |
| `Whitelabel Error Page` after a working deploy | Your app started, but a route is broken | Check Render logs for stack traces. Usually a JPA/Hibernate dialect mismatch with Postgres. |

---

## Security checklist

- [ ] `application-prod.properties` references `${JDBC_DATABASE_URL}` — never a hardcoded URL
- [ ] No passwords or secrets in any committed file (run `git log -p | grep -i password` to double-check)
- [ ] No secrets pasted into AI chat logs, public Discord, screenshots, or screen recordings
- [ ] If a credential is exposed, **rotate it immediately:** Neon → Roles → Reset password, then update the `JDBC_DATABASE_URL` env var on Render
- [ ] Repo is public **only** because it contains no secrets

---

## What this SOP does NOT cover

- Custom domain names (paid)
- HTTPS certificates (Render auto-handles via Let's Encrypt — no action needed)
- Email, file uploads, OAuth, payment processing — out of scope for the final project
- Scaling beyond one free dyno (paid plans)

---

## Submission checklist

- [ ] Code in a public GitHub repo
- [ ] Live deployment URL on Render
- [ ] `README.md` includes the live URL near the top
- [ ] App demonstrates CRUD + persistence (add data, sleep 16 min, reload, data still there)
- [ ] Tests pass (`mvn test`)

---

## Reference

Working repo to copy patterns from: https://github.com/benlambm/tbr-app
Live demo: https://tbr-app.onrender.com

The hard parts are already debugged. Read the source. Steal liberally.

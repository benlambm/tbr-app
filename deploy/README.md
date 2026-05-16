# VPS deployment — fat JAR + systemd

Single-VPS production stack. nginx (already running) reverse-proxies to a
Spring Boot fat JAR managed by systemd. No Docker.

## What's in this directory

| File | Goes to | Purpose |
|---|---|---|
| `tbr.service` | `/etc/systemd/system/tbr.service` | systemd unit definition |
| `tbr-deploy.sudoers` | `/etc/sudoers.d/tbr-deploy` | NOPASSWD rights for the GHA deploy user |
| `.env.example` | template for `/opt/tbr/.env` | env vars (Spring profile, DB URL) |

## One-time VPS setup

```bash
# Java 25 (Eclipse Temurin)
sudo apt install temurin-25-jdk

# Service user — no shell, no home, no login
sudo useradd -r -s /usr/sbin/nologin -d /opt/tbr -c "TBR app" tbr

# Working dirs
sudo mkdir -p /opt/tbr/{incoming,current}
sudo chown -R tbr:tbr /opt/tbr

# /opt/tbr/incoming/ is the SCP staging area. The deploy user writes the
# new JAR there, then sudo-chowns it to tbr:tbr and the tbr user moves it
# into place. Both users need to write to this directory:
#   - deploy (owner) to drop the JAR via SCP
#   - tbr (group)    to remove it during the atomic swap
sudo chown deploy:tbr /opt/tbr/incoming
sudo chmod 775       /opt/tbr/incoming

# Env file (edit JDBC URL after copying)
sudo install -o tbr -g tbr -m 640 deploy/.env.example /opt/tbr/.env
sudoedit /opt/tbr/.env

# systemd unit
sudo install -o root -g root -m 644 deploy/tbr.service /etc/systemd/system/tbr.service
sudo systemctl daemon-reload
sudo systemctl enable tbr
# (don't start yet — no app.jar exists. The first deploy will SCP one in.)

# Sudoers for the GHA deploy user
sudo visudo -f /etc/sudoers.d/tbr-deploy
# (paste contents of deploy/tbr-deploy.sudoers; visudo validates syntax)
```

## nginx

Front the service with nginx as you already do. Minimum config snippet,
inside your existing `server {}` block:

```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host              $host;
    proxy_set_header X-Real-IP         $remote_addr;
    proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;

    # Mask the 3-5s restart window during deploys
    proxy_next_upstream error timeout http_502 http_503 http_504;
    proxy_connect_timeout 2s;
    proxy_read_timeout    30s;
}
```

## Operations

```bash
# Status / logs
sudo systemctl status tbr
sudo journalctl -u tbr -f          # tail
sudo journalctl -u tbr --since '1 hour ago'

# Restart
sudo systemctl restart tbr

# Rollback to the previous JAR (kept by the deploy script as app.jar.prev)
sudo -u tbr mv /opt/tbr/app.jar.prev /opt/tbr/app.jar
sudo systemctl restart tbr
```

## GitHub Actions secrets

The deploy workflow expects three repo secrets:

| Secret | Value |
|---|---|
| `VPS_HOST` | hostname or IP of the VPS |
| `VPS_USER` | the `deploy` user (matches the sudoers file) |
| `VPS_SSH_KEY` | private SSH key authorized for `deploy@VPS_HOST` |

Set them with `gh secret set VPS_HOST -b "..."`, etc.

## CDS archive

The systemd unit boots with `-XX:SharedArchiveFile=/opt/tbr/app.jsa`. The
deploy workflow regenerates that archive against the new JAR before each
restart, so cold-start time stays in the ~3s range. If `app.jsa` is
missing the JVM ignores the flag and boots normally — there is no
catastrophic failure mode.

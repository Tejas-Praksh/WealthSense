# Render.com (free web services + Docker)

Deploy each **Spring Boot** service as a **Web Service** using the repo **Dockerfile** per module.

## Per-service checklist

1. [render.com](https://render.com) → **New** → **Web Service**.
2. Connect **GitHub** repository.
3. Settings (example — **user-service**):
   - **Name:** `wealthsense-user-service`
   - **Branch:** `main`
   - **Runtime:** **Docker**
   - **Dockerfile path:** `./user-service/Dockerfile`
   - **Root directory / Docker build context:** set to **`user-service`** so `COPY target/*.jar app.jar` resolves (each service Dockerfile expects its own `target/` directory). Alternatively use a **root** Dockerfile that multi-stage builds and copies the correct JAR.

4. **Instance type:** Free (sleeps after inactivity).

5. **Environment** (set in dashboard; see `deployment/all-env-vars.md`):
   - `SPRING_PROFILES_ACTIVE=production`
   - `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
   - `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` + TLS (see `application-production.yml`)
   - `KAFKA_BOOTSTRAP_SERVERS` + SASL properties for Upstash
   - `MONGO_URI` (only for services that use MongoDB)
   - `JWT_SECRET`, `ENCRYPTION_KEY`, `WEBHOOK_SECRET`
   - `CLAUDE_API_KEY`, `GEMINI_API_KEY` (AI / advisor)
   - `FRONTEND_URL` — your Vercel URL for CORS on gateway

6. **Health check path:** `/actuator/health`

7. Repeat for all services:

| Service | Dockerfile path | Port (internal) |
|---------|-----------------|-----------------|
| api-gateway | `./api-gateway/Dockerfile` | 8080 |
| user-service | `./user-service/Dockerfile` | 8081 |
| transaction-service | `./transaction-service/Dockerfile` | 8082 |
| fraud-detection-service | `./fraud-detection-service/Dockerfile` | 8083 |
| decision-engine-service | `./decision-engine-service/Dockerfile` | 8084 |
| notification-service | `./notification-service/Dockerfile` | 8085 |
| ai-advisor-service | `./ai-advisor-service/Dockerfile` | 8086 |
| investment-service | `./investment-service/Dockerfile` | 8087 |

Expose **only** the port your container listens on (see each Dockerfile `EXPOSE`).

## API Gateway → service URLs

On free Render, each service gets its own hostname. Point **gateway routes** (env vars already used in code) to Render URLs, e.g.:

```text
USER_SERVICE_URL=https://wealthsense-user-service.onrender.com
TRANSACTION_SERVICE_URL=https://wealthsense-transaction-service.onrender.com
...
```

Rebuild/redeploy gateway after all backends exist.

## Deploy hooks (GitHub Actions)

Each Render service: **Settings → Deploy Hook** → copy URL.

- Option A: one secret per service hook and a small workflow matrix (recommended).
- Option B: single hook if you use a **Render Blueprint** that redeploys the stack (advanced).

The sample workflow uses `RENDER_DEPLOY_HOOK_URL` for **one** hook; duplicate the step or use a matrix once hooks exist.

## Live demo URLs

After deploy, your **live** URLs will look like:

- `https://wealthsense-api-gateway.onrender.com`
- `https://wealthsense-user-service.onrender.com`
- … (copy from Render dashboard)

Paste them into `README.md` and `wealthsense-frontend` env vars (`VITE_API_URL`).

## Free tier caveats

- **Cold start:** first request after sleep can take **30–60+ seconds**.
- **Sleep:** after ~15 minutes idle on free tier.
- Use **UptimeRobot** (see `deployment/uptimerobot-setup.md`) to ping `/actuator/health` on a **14-minute** interval for demos (respect Render fair-use; not a substitute for paid always-on).

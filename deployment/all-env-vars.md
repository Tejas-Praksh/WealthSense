# Environment variables (never commit values)

Set these in **Render**, **Vercel**, and **GitHub Actions secrets** only.

## Generate secrets (local Node)

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Use for:

- **JWT signing** (`JWT_SECRET`) — must be long / high entropy (jjwt HS256).
- **AES encryption** (`ENCRYPTION_KEY`) — **32-byte** secret, Base64-encoded (matches `security-lib` expectations).
- **Webhooks** (`WEBHOOK_SECRET`) — hex or strong random string.

## Core (all Spring services that run in production)

| Variable | Purpose |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `production` |
| `POSTGRES_URL` | JDBC URL to Supabase (sslmode=require) |
| `POSTGRES_USER` | Usually `postgres` |
| `POSTGRES_PASSWORD` | DB password |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Upstash Redis (TLS on) |
| `KAFKA_BOOTSTRAP_SERVERS` | Upstash Kafka brokers |
| `SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL` | `SASL_SSL` |
| `SPRING_KAFKA_PROPERTIES_SASL_MECHANISM` | e.g. `SCRAM-SHA-256` |
| `SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG` | JAAS line from Upstash |
| `MONGO_URI` | Atlas SRV string (Mongo-using services only) |
| `JWT_SECRET` | JWT HMAC secret |
| `ENCRYPTION_KEY` | Base64 32-byte AES key |
| `WEBHOOK_SECRET` | Webhook HMAC / shared secret |
| `FRONTEND_URL` | Vercel URL for gateway CORS |

## Optional / feature-specific

| Variable | Service |
|----------|---------|
| `CLAUDE_API_KEY` | ai-advisor-service |
| `GEMINI_API_KEY` | if integrated |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | payments (if used) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | notification-service (SMTP) |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | user-service OAuth2 |
| `USER_SERVICE_URL`, `TRANSACTION_SERVICE_URL`, … | api-gateway route targets |

## Gateway inter-service URLs (Render)

After each service deploys, set on **api-gateway**:

```text
USER_SERVICE_URL=https://...
TRANSACTION_SERVICE_URL=https://...
FRAUD_SERVICE_URL=https://...
DECISION_ENGINE_SERVICE_URL=https://...
NOTIFICATION_SERVICE_URL=https://...
AI_ADVISOR_SERVICE_URL=https://...
INVESTMENT_SERVICE_URL=https://...
```

## Grafana Cloud (free)

Create a stack at [grafana.com](https://grafana.com), then add **Prometheus remote_write** or **OTLP** credentials as separate env vars when you wire exporters (optional follow-up).

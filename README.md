# WealthSense 💰
### Real-Time Financial Decision Engine

> "Not a tracker. A financial decision engine."

[![CI](https://github.com/tejas-acharya/wealthsense/actions/workflows/ci.yml/badge.svg)](https://github.com/tejas-acharya/wealthsense/actions/workflows/ci.yml)
[![Coverage](https://img.shields.io/badge/coverage-80%25-green)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

## Problem statement

80 crore Indians use UPI daily but have zero financial intelligence. No fraud protection. No personalized AI advice. No real-time decision making.

WealthSense is not an expense tracker. It is a **real-time financial decision engine** — every rupee movement triggers intelligent decisions automatically in under 200ms.

## Live demo (placeholders)

These URLs are placeholders until your deployment is wired up:

- **Frontend:** https://wealthsense.vercel.app  
- **API (gateway):** https://wealthsense-api.onrender.com  
- **Swagger (example):** https://wealthsense-api.onrender.com/swagger-ui.html  

Locally: API Gateway at `http://localhost:8080`; per-service Swagger on each service port (see below).

## Performance benchmarks

Load tests, smoke scripts, and the **results template** live under [`performance/`](performance/README.md).  
**Measured** latency and throughput belong in [`performance/RESULTS.md`](performance/RESULTS.md) only after you run JMeter or the shell scripts against a **live** environment (see the honesty log there for a sample probe when Render was suspended).

High-level **design targets** (not a substitute for measurements) include sub‑500 ms auth p99 at the gateway under controlled load, 10k+ financial events per minute at scale in the streaming path, and chat responses bounded by upstream LLM latency.

## System architecture

```
┌─────────────────────────────────────┐
│         React 18 Frontend          │
│   (wealthsense-frontend / Vite)    │
└──────────────┬──────────────────────┘
               │ HTTPS
┌──────────────▼──────────────────────┐
│         API Gateway :8080            │
│  JWT │ Rate limit │ Idempotency      │
│  Correlation ID │ Circuit breaker    │
└──┬────┬────┬────┬────┬────┬────┬─────┘
   │    │    │    │    │    │    │
   ▼    ▼    ▼    ▼    ▼    ▼    ▼
 User Trans Fraud Dec  Notif  AI  Invest
 8081 8082 8083 8084 8085 8086 8087
   │    │    │    │    │    │    │
   └────┴────┴────┴────┴────┴────┘
               │
┌──────────────▼──────────────┐
│       Apache Kafka         │
│      Event streaming       │
└──────────────┬────────────┘
               │
     ┌─────────┼─────────┐
     ▼         ▼         ▼
 PostgreSQL   MongoDB    Redis
 (per svc)   (AI/dec.)  cache / idempotency
```

## Event flow

```
User makes UPI payment
        │
        ▼
API Gateway → JWT validation
        │
        ▼
Idempotency check (Redis)
        │
        ▼
Transaction Service
→ Save to DB + outbox (atomic, same transaction)
        │
        ▼
Outbox Publisher → Kafka (`transaction-events`)
        │
   ┌────┴────┐
   ▼         ▼
Fraud      Decision
Detection   Engine
(~85ms)    (SAGA steps)
   │         │
   └────┬────┘
        ▼
Notification Service
(Email / push / Kafka + RabbitMQ)

Total: <200ms (design target)
```

## Tech stack

### Backend

| Technology | Purpose |
|------------|---------|
| Java 21 | Core language |
| Spring Boot 3.2.5 | Framework |
| Spring Cloud Gateway | API gateway (reactive) |
| Apache Kafka | Event streaming |
| RabbitMQ | Priority notifications |
| Redis | Cache, rate limiting, idempotency |
| PostgreSQL | Transactional data (per bounded context) |
| MongoDB | AI / decision context |
| Spring AI | Claude integration (AI advisor) |
| Resilience4j | Circuit breaker, retry, bulkhead |
| SpringDoc OpenAPI | API docs (`/swagger-ui.html`) |
| Custom Java fraud rules | Parallel `FraudRule` scoring + `RiskScoringService` |

### Architecture patterns

| Pattern | Where it shows up |
|---------|-------------------|
| Outbox | Transaction service → Kafka without dual-write races |
| SAGA | Decision engine orchestration steps |
| Event-driven | Kafka topics between services |
| Circuit breaker | Gateway + Resilience4j |
| Idempotency | Redis-backed idempotency keys on writes |

### Security

| Feature | Implementation |
|---------|----------------|
| Encryption | AES-256-GCM (`security-lib`) |
| Webhooks | HMAC verification patterns |
| Auth | JWT (+ OAuth2 client on user service) |
| Access | RBAC (`@PreAuthorize`) |
| Idempotency | Redis keys for duplicate prevention |
| Audit | `@Auditable` AOP (`security-lib`) |

### Frontend

`wealthsense-frontend` — React 18, Vite, Tailwind (see that package for exact UI stack).

### DevOps and monitoring

Docker, Docker Compose, Kubernetes manifests under `k8s/`, GitHub Actions under `.github/workflows/`, Prometheus/Grafana configs under `monitoring/`, distributed tracing (Zipkin/Micrometer), Spring Actuator health endpoints.

## Project structure

```
wealthsense/
├── api-gateway/
├── user-service/
├── transaction-service/
├── fraud-detection-service/
├── decision-engine-service/
├── notification-service/
├── ai-advisor-service/
├── investment-service/
├── common-library/
├── security-lib/
├── wealthsense-frontend/
├── k8s/
├── monitoring/
├── aws/
├── .github/workflows/
├── docs/
├── scripts/
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Quick start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker Desktop
- Node.js 22+ (for frontend)

### Run locally

```bash
# Clone repo
git clone https://github.com/tejas-acharya/wealthsense.git
cd wealthsense

# Environment
cp .env.example .env
# Edit .env with your secrets (never commit real values)

# Infrastructure
docker-compose up -d

# Build all services
mvn clean package -DskipTests

# Start all Spring Boot apps (from repo root; Git Bash / WSL / macOS / Linux)
bash scripts/start-all.sh

# Frontend
cd wealthsense-frontend
npm install
npm run dev
```

### Service URLs (local)

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 (default Vite) |
| API Gateway | http://localhost:8080 |
| User Swagger | http://localhost:8081/swagger-ui.html |
| Transaction Swagger | http://localhost:8082/swagger-ui.html |
| Grafana | http://localhost:3001 (if enabled in compose) |
| Kibana | http://localhost:5601 (if ELK stack running) |
| Zipkin | http://localhost:9411 |
| Prometheus | http://localhost:9090 (if exposed) |
| RabbitMQ UI | http://localhost:15672 |

## Security features

- AES-256-GCM encryption (authenticated)
- HMAC-style webhook verification where configured
- JWT access tokens + refresh rotation (`user-service`)
- OAuth2 Google login (optional, configured in `user-service`)
- RBAC for admin routes
- Idempotency keys to prevent double charges on writes
- Rate limiting at the gateway
- Audit hooks via AOP on sensitive operations

## Key engineering decisions

### Why Kafka over synchronous REST for money events?

Financial events must survive downstream outages. Kafka retains messages until consumers catch up; a naive REST chain fails closed when one hop is down.

### Why outbox?

If the database commit succeeds and the process crashes before a Kafka `send`, the event is lost. The **transactional outbox** keeps the publish in the same DB transaction as the business write.

### Why SAGA over 2PC?

Two-phase commit holds locks across services — painful at high TPS. SAGA models compensating steps and fits async, message-driven flows.

### Why PostgreSQL and MongoDB?

OLTP money paths want ACID (PostgreSQL). High-volume, flexible context (AI conversations, decision traces) maps well to MongoDB in this codebase.

## Testing

```bash
# All tests
mvn test

# Verify (includes checks your CI may run)
mvn verify

# Coverage (when JaCoCo is enabled on modules)
mvn test jacoco:report
# Then open: user-service/target/site/jacoco/index.html (per module)
```

Stack: JUnit 5, Mockito, Testcontainers (integration tests where enabled).

## Business context

| Metric | Value |
|--------|-------|
| Target market | 80 crore UPI users |
| Problem | Zero financial intelligence at point of spend |
| Architecture scale | Microservices, horizontal scaling, K8s-ready |

## Author

**Tejas** — Java backend developer  

- LinkedIn: [linkedin.com/in/tejas-acharya/](https://www.linkedin.com/in/tejas-acharya/)  
- GitHub: [github.com/tejas-acharya](https://github.com/tejas-acharya)  

> If your GitHub username or org differs, update badge and clone URLs in this README.

## License

MIT License — see [LICENSE](LICENSE) if present in the repo.

---

*Built for 80 crore Indians who deserve better financial tools.*

# Architecture deep dive

This document reflects the **WealthSense** codebase as implemented: Java 21, Spring Boot 3.2.5, Spring Cloud Gateway, Kafka, PostgreSQL, MongoDB, Redis, RabbitMQ, and shared libraries `common-library` and `security-lib`.

## Architecture decision records

### ADR-001: Event-driven core (Kafka)

**Decision:** Use Kafka for transaction lifecycle and downstream reactions (fraud, decisions, notifications).

**Reason:** Financial events must not be lost when a consumer is temporarily down; the broker buffers and replays.

**Result:** Decoupled scaling; topics defined in `common-library` (`KafkaTopics`).

### ADR-002: SAGA-style orchestration

**Decision:** Decision engine coordinates multi-step flows (see `decision-engine-service` saga packages).

**Reason:** Avoid 2PC-style locking across microservices under load.

**Result:** Async steps with explicit orchestration code.

### ADR-003: Transactional outbox

**Decision:** Transaction service persists business data and **outbox rows** in one transaction; a publisher pushes to Kafka.

**Reason:** Eliminates “DB committed, message never sent” after a crash.

**Result:** `OutboxRepository`, `OutboxPublisherService`, `TransactionEventProducer`.

### ADR-004: Read paths vs write paths

**Decision:** Redis used for caching, rate limiting, and idempotency (gateway + `security-lib` idempotency).

**Reason:** Protect the write database and provide fast idempotency lookups.

**Result:** Redis in Docker Compose and Kubernetes configs; idempotency service backed by Redis.

### ADR-005: AES-256-GCM (not CBC)

**Decision:** Authenticated encryption in `security-lib`.

**Reason:** GCM gives confidentiality + integrity; tampering is detectable.

**Result:** `AESEncryptionService` and related utilities.

## Service responsibilities

| Service | Port | Responsibility |
|---------|------|----------------|
| api-gateway | 8080 | Routing, JWT filter, rate limit, idempotency, circuit breaker to downstream URLs |
| user-service | 8081 | Registration, login, refresh, profiles, OAuth2 client hooks |
| transaction-service | 8082 | Transactions, accounts, outbox, Kafka publication |
| fraud-detection-service | 8083 | Kafka consumer, fraud rules, risk scoring, fraud alerts |
| decision-engine-service | 8084 | Kafka-driven saga / decisions |
| notification-service | 8085 | Kafka + RabbitMQ + mail channels |
| ai-advisor-service | 8086 | Spring AI / chat, insights |
| investment-service | 8087 | SIP, goals, tax, portfolio APIs |

## Kafka topics (from `KafkaTopics`)

| Topic constant | Typical producer | Typical consumer |
|----------------|------------------|------------------|
| `transaction-events` | transaction-service (via outbox publisher) | fraud-detection, decision-engine |
| `fraud-alerts` | fraud-detection-service | decision-engine |
| `decision-events` | decision-engine-service | notification-service |
| `notification-events` | various | notification-service |
| `audit-events` | services emitting audit | audit pipeline (as configured) |
| `outbox-events` | internal / diagnostics | as configured |
| `transaction-events.DLQ` | failed consumer handling | operations / replay |
| `fraud-alerts.DLQ` | failure path | operations |
| `notification-events.DLQ` | failure path | operations |

Exact wiring is in each service’s Kafka configuration and consumers.

## Security architecture

1. **Ingress:** Client calls **API Gateway** over HTTPS (production).
2. **Gateway:** JWT validation, correlation ID, rate limiting, idempotency where applied.
3. **Downstream:** Services may accept `X-User-ID` and other headers from the gateway; **configure trust boundaries** for production (mTLS or network policy), not only headers.

Internal services expose **Spring Actuator** health endpoints for orchestration and probes (`/actuator/health`, liveness/readiness where enabled).

## Data stores

- **PostgreSQL:** Primary OLTP per service (users, transactions, fraud, investments as configured).
- **MongoDB:** AI advisor and decision-engine document-style data.
- **Redis:** Cache, session-style data, idempotency, gateway rate limiting.

## Related docs

- [API.md](API.md) — endpoint summary and conventions  
- [Kubernetes deployment guide](../k8s/README.md) — cluster apply order and probes  

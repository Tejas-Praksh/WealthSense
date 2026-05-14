# Upstash (free Redis + Kafka)

## Redis

1. [upstash.com](https://upstash.com) → **Console** → **Redis** → **Create database**.
2. Name: `wealthsense-redis` (example).
3. Region: **ap-south-1** (Mumbai) when available.
4. Copy for Spring Boot **Lettuce** (TCP):
   - **Endpoint** (host)
   - **Port** (often `6379` with TLS)
   - **Password**

Set in Render:

```bash
REDIS_HOST=<endpoint>
REDIS_PORT=6379
REDIS_PASSWORD=<password>
```

Enable TLS in app: `application-production.yml` sets `spring.data.redis.ssl.enabled=true`.  
If Upstash gives a `rediss://` URL, you can instead set `SPRING_DATA_REDIS_URL` (Spring Boot supports it) and omit host/port/password.

**REST API** (Upstash REST) is optional; this codebase uses the **Redis protocol** via Spring Data Redis.

## Kafka

1. Upstash Console → **Kafka** → **Create cluster** (e.g. `wealthsense-events`).
2. Region: **ap-south-1** when available.
3. Create topics (match `com.wealthsense.common.constants.KafkaTopics`):

   - `transaction-events`
   - `fraud-alerts`
   - `decision-events`
   - `notification-events`
   - `audit-events`
   - `outbox-events`
   - `transaction-events.DLQ`
   - `fraud-alerts.DLQ`
   - `notification-events.DLQ`

4. Copy **bootstrap servers** and **SASL** username/password (Upstash UI shows exact env vars).

Spring Boot example env (names may vary by Upstash docs):

```bash
KAFKA_BOOTSTRAP_SERVERS=<broker1:9092,...>
SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL=SASL_SSL
SPRING_KAFKA_PROPERTIES_SASL_MECHANISM=SCRAM-SHA-256
SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username="..." password="...";
```

Use Render **secret** files or encrypted env for `SASL_JAAS_CONFIG` (escape quotes carefully).

## Free tier expectations

- Daily command / message caps apply — fine for demos and light traffic.
- For production scale, move to paid tiers or self-hosted Kafka.

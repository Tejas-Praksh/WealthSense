# WealthSense Monitoring Stack

## Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://localhost:3001 | admin / admin123 |
| Prometheus | http://localhost:9090 | none |
| Kibana | http://localhost:5601 | none |
| Zipkin | http://localhost:9411 | none |
| Elasticsearch | http://localhost:9200 | none |
| Logstash TCP | localhost:5000 | — |
| RabbitMQ UI | http://localhost:15672 | admin / admin123 |

## Quick Start

```bash
# Start the full monitoring stack (from project root)
docker-compose up -d prometheus grafana elasticsearch kibana logstash zipkin

# Check all service health
bash monitoring/health-check.sh

# View logs in Kibana
# 1. Open http://localhost:5601
# 2. Create index pattern: wealthsense-logs-*
# 3. Set time field: @timestamp
```

## Architecture

```
Services → Logstash:5000 (TCP/JSON) → Elasticsearch → Kibana
Services → /actuator/prometheus     → Prometheus    → Grafana
Services → Zipkin HTTP reporter     → Zipkin UI
```

## Grafana Dashboards

After starting, the **WealthSense Overview** dashboard auto-loads showing:

- **Row 1 — System Health**: Service UP/DOWN stat panels (green/red)
- **Row 2 — Request Metrics**: RPS, error rate %, P99/P95 response time
- **Row 3 — Business Metrics**: Transactions/min, fraud alerts/hr, active users, AI queries
- **Row 4 — Infrastructure**: JVM heap, CPU, Kafka lag, Redis memory
- **Row 5 — Circuit Breakers**: CB state table, fallback rate, retry attempts
- **Row 6 — Database**: PostgreSQL connections, query time, MongoDB ops/s, Redis hit rate

## Alerts Configured

| Alert | Trigger | Severity |
|-------|---------|----------|
| ServiceDown | `up == 0` for 1m | Critical |
| HighErrorRate | 5xx rate > 10% for 2m | Warning |
| HighResponseTime | Avg > 2s for 5m | Warning |
| KafkaConsumerLag | Lag > 1000 for 5m | Warning |
| HighMemoryUsage | JVM heap > 85% for 5m | Warning |
| CircuitBreakerOpen | CB state = open for 1m | Critical |

## Log Shipping (per service)

Copy `monitoring/logback/logback-spring.xml` to each service at:
```
src/main/resources/logback-spring.xml
```

Add to each service `pom.xml`:
```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
</dependency>
```

## Distributed Tracing (Zipkin)

Add to each service `application.yml`:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
spring:
  zipkin:
    base-url: http://zipkin:9411
```

## Custom Business Metrics

Add to `transaction-service` — inject `MeterRegistry`:
```java
Counter.builder("wealthsense.transactions.total")
  .tag("type", type.name()).tag("status", status.name())
  .register(meterRegistry).increment();

Timer.builder("wealthsense.fraud.check.duration")
  .register(meterRegistry);

Gauge.builder("wealthsense.users.active", activeCount, AtomicLong::get)
  .register(meterRegistry);
```

## File Structure

```
monitoring/
├── prometheus/
│   ├── prometheus.yml        ← Scrape config (all 8 services)
│   └── alert_rules.yml       ← 6 alert rules
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/
│   │   │   └── datasource.yml    ← Auto-provisions Prometheus + ES
│   │   └── dashboards/
│   │       └── dashboard.yml     ← Auto-loads dashboard JSONs
│   └── dashboards/
│       └── wealthsense-overview.json  ← Main dashboard
├── elk/
│   └── logstash/
│       └── logstash.conf     ← TCP input → ES output pipeline
├── logback/
│   └── logback-spring.xml    ← Shared logback config (async TCP)
├── health-check.sh           ← Verify all services UP
└── README.md                 ← This file
```

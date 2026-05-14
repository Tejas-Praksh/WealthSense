# WealthSense Performance Tests

Load and smoke tests for the API Gateway and downstream services. **Benchmark tables belong in `RESULTS.md` only after you run the scripts against a live deployment** and paste measured values (no fabricated latency).

## Setup

1. Install **JMeter 5.6+** from [Apache JMeter](https://jmeter.apache.org/download_jmeter.cgi).
2. Optional plugins (for advanced dashboards / server metrics):
   - [JMeter Plugins Manager](https://jmeter-plugins.org/wiki/PluginsManager/)
   - PerfMon Metrics Collector
   - Custom Thread Groups

## Test scenarios

### 1. Auth load test (`test-plans/auth-load-test.jmx`)

- **Threads:** `-Jthreads` (default `100`)
- **Ramp-up:** `-Jramp` seconds (default `60`)
- **Duration:** `-Jduration` seconds (default `300`)
- **Flow:** each thread registers once (`perf_{threadNum}@example.com`), then loops **login → GET profile** until the scheduler stops the group.
- **SLO (documentary):** p99 &lt; 500 ms for login + profile (validate from HTML report or JTL).

### 2. Transaction load test (`test-plans/transaction-load-test.jmx`)

- **Threads:** default `50`, **duration** default `300` s.
- **Flow:** one-time **register → login → create account**, then loop **POST /api/v1/transactions** with a fresh `X-Idempotency-Key` per request (`${__UUID}`).
- **SLO:** sustained **10k+ transactions/min** is a product goal; derive actual TPS from the JTL / HTML dashboard.

### 3. Fraud detection test (`test-plans/fraud-detection-test.jmx`)

The fraud worker is **Kafka-driven** and does not expose a public “fraud score” HTTP API. This plan load-tests **`GET /actuator/health`** on the **fraud service host** (or any URL you pass), as a **service availability / latency** probe.

- **True** “Kafka → fraud decision” latency needs **metrics/traces** (e.g. Prometheus, Grafana, distributed trace span duration), not this JMeter file alone.
- **Hosts:** `-Jfraud_protocol`, `-Jfraud_host`, `-Jfraud_port` (see `scripts/run-tests.sh`).

### 4. AI advisor test (`test-plans/ai-advisor-test.jmx`)

- **Threads:** default `10`, **duration** default `300` s.
- **Flow:** register once per thread, login, then loop **POST /api/v1/ai/chat** with a short message.
- **SLO:** chat p99 &lt; 3 s depends on upstream LLM latency and quotas.

## JMeter properties

All plans accept:

| Property | Meaning | Typical value |
|----------|---------|-----------------|
| `protocol` | `http` or `https` | `https` |
| `host` | Gateway host | your Render hostname |
| `port` | Gateway port | `443` |
| `threads` | Concurrent threads | scenario-specific |
| `ramp` | Ramp-up (seconds) | `30`–`60` |
| `duration` | Scheduler duration (seconds) | `300` |

Fraud plan additionally uses `fraud_protocol`, `fraud_host`, `fraud_port`.

## Running JMeter tests

From this directory (`performance/`), with `jmeter` on your `PATH`:

```bash
bash scripts/run-tests.sh 'https://YOUR-GATEWAY.example.com'
```

Results are written under `results/<timestamp>/` as `.jtl` files.

## HTML reports

```bash
bash scripts/generate-report.sh results/20260101_120000
```

Then open `results/<timestamp>/auth-report/index.html` (and sibling `transaction-report`, etc.) in a browser.

## API smoke tests (curl, no JMeter)

Useful when Render free tier **cold starts** make long JMeter runs flaky, or when you only need **HTTP status** coverage:

```bash
bash api-tests/test-all-endpoints.sh 'https://YOUR-GATEWAY.example.com'
```

## Latency spot-checks

```bash
bash measure-latency.sh 'https://YOUR-GATEWAY.example.com'
```

Optional environment variables:

- `LATENCY_EMAIL` / `LATENCY_PASSWORD` — measure **login** round-trip.
- `LATENCY_TOKEN` — measure authenticated endpoints (e.g. SIP calculator).

## Recording results

After a real run, copy summary numbers into `RESULTS.md` (see the template there). Update the root `README.md` performance table only with values backed by those runs.

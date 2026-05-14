# WealthSense performance results

This file is the **single source of truth** for benchmark numbers that appear on your resume or in the root `README.md`. **Do not invent latency or throughput.** Fill the tables only from:

- JMeter HTML reports / `.jtl` files under `performance/results/<timestamp>/`, and/or  
- Output of `performance/measure-latency.sh` and `performance/api-tests/test-all-endpoints.sh`.

## Test environment

| Field | Value |
|--------|--------|
| Date | *(YYYY-MM-DD — set when you run tests)* |
| API base URL | *(e.g. `https://…onrender.com`)* |
| Client region | *(e.g. India — your ISP / city)* |
| JMeter version | *(e.g. 5.6.3)* |
| Notes | *(cold starts, rate limits, LLM quota, etc.)* |

## Connectivity probe (automated honesty log)

The following is a **real outcome** from a quick probe on **2026-05-14** (PowerShell `Invoke-WebRequest`, client environment: Windows):

| URL | HTTP status | Note |
|-----|----------------|------|
| `https://wealthsense-api.onrender.com/actuator/health` | 503 | HTML body: “Service Suspended” |
| `https://wealthsense-api.onrender.com/swagger-ui.html` | 503 | Same |
| `https://wealthsense-api-gateway.onrender.com/swagger-ui.html` | 404 | Host responded, route not found |

Because the deployed endpoints above did not return successful application responses during that probe, **no trustworthy latency or TPS samples were collected for production on that date.** Re-run the scripts after services are live and replace the tables below.

## API response times (fill from JMeter or `measure-latency.sh`)

| Endpoint | Avg (ms) | P95 (ms) | P99 (ms) | Source |
|----------|----------|----------|----------|--------|
| `GET /actuator/health` | TBD | TBD | TBD | JMeter / curl |
| `POST /api/v1/auth/login` | TBD | TBD | TBD | JMeter auth plan |
| `GET /api/v1/users/profile` | TBD | TBD | TBD | JMeter auth plan |
| `POST /api/v1/transactions` | TBD | TBD | TBD | JMeter transaction plan |
| `GET /api/v1/transactions` | TBD | TBD | TBD | Optional |
| Fraud pipeline (Kafka → decision) | TBD | TBD | TBD | **Requires metrics/traces** (not HTTP-only; see `performance/README.md`) |
| `POST /api/v1/ai/chat` | TBD | TBD | TBD | JMeter AI plan |
| `POST /api/v1/investments/sip/calculate` | TBD | TBD | TBD | `measure-latency.sh` with `LATENCY_TOKEN` |

## Throughput (fill from JTL / HTML dashboard)

| Metric | Result | How measured |
|--------|--------|----------------|
| Peak HTTP TPS (transactions create) | TBD | JMeter `transaction-results.jtl` |
| Sustained transactions/min | TBD | Same |
| Concurrent threads used | TBD | `-Jthreads` |
| Cache hit rate | TBD | Application metrics (if exported) |
| Kafka publish rate | TBD | Broker / app metrics (if exported) |

## How to refresh this document

1. Deploy a healthy gateway + dependencies on Render (or your target environment).  
2. From `performance/`:  
   - `bash scripts/run-tests.sh 'https://YOUR-GATEWAY'`  
   - `bash scripts/generate-report.sh results/<timestamp>`  
3. Optionally:  
   - `LATENCY_EMAIL=… LATENCY_PASSWORD=… LATENCY_TOKEN=… bash measure-latency.sh 'https://YOUR-GATEWAY'`  
4. Copy percentiles from the HTML dashboards into the tables above and commit the updated `RESULTS.md`.

## Product SLOs (targets, not measurements)

These are **engineering targets** for design discussions; they are **not** substitute values for the TBD cells above until measured.

- Auth (login + profile): p99 &lt; 500 ms  
- Transaction writes: design goal 10k+ transactions/min at scale  
- Fraud: decision latency target &lt; 100 ms **in the streaming path** (validate with tracing/metrics)  
- AI chat: p99 &lt; 3 s (dominated by external LLM latency)

# Grafana Cloud (free tier)

1. Sign up at [grafana.com](https://grafana.com/auth/sign-up/create-user).
2. Create a **Free** stack (retention limits apply).
3. Obtain **Prometheus remote_write** endpoint + user / password (or OTLP for traces) from the stack **Details** page.
4. Point **Prometheus** (self-hosted or agent) or **Micrometer OTLP** registry at Grafana Cloud using env vars on a small collector or on a single “metrics” sidecar if you add one later.

This repo exposes **Actuator Prometheus** on each service (`/actuator/prometheus` when enabled). Scraping from the public internet is usually blocked — prefer **Render private networking** or a **push** model (remote_write) with credentials stored only in Render secrets.

No Grafana config is committed here; wire dashboards after your first successful deploy.

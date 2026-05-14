# WealthSense screenshots guide

Use these captures for the root `README.md`, portfolio site, and LinkedIn carousels. Prefer **real data** (your own account or anonymized fixtures) over placeholder text.

## 1. Dashboard

- Balance / net-worth style card (if applicable).  
- Spending trend or category chart.  
- Recent transactions list.

## 2. AI Advisor chat

- A short user question about money in the Indian context.  
- A coherent assistant reply (Claude / Spring AI), showing the product tone.

## 3. Transactions

- Filters (date, type, category) in use.  
- List + at least one chart if the UI exposes it.

## 4. Investments

- SIP calculator with inputs and projected output.  
- Goals list or progress if available.

## 5. Grafana (if deployed)

- JVM or HTTP latency panels.  
- Business counters (transactions, decisions) if instrumented.

## 6. Swagger UI

- Gateway or a representative service showing grouped tags.  
- “Try it out” on a **non-destructive** GET, with a valid bearer token if needed.

## 7. Architecture diagram

- Microservices and the API Gateway.  
- Kafka topics / consumers in the money path.  
- PostgreSQL / MongoDB / Redis in the right roles.

**Tools**

- Windows: **Win + Shift + S** (Snipping Tool).  
- Full-page: Firefox **Screenshot** in devtools, or a browser extension.  
- Diagrams: [Excalidraw](https://excalidraw.com) or draw.io.

## File naming (suggested)

Store under `docs/images/` (or your site’s asset folder) with stable names, for example:

- `dashboard.png`  
- `ai-advisor.png`  
- `transactions.png`  
- `investments.png`  
- `grafana-overview.png`  
- `swagger-gateway.png`  
- `architecture.png`

Do not commit secrets, full card numbers, or live API keys in screenshots.

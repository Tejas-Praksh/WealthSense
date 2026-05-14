# WealthSense API Documentation

## Base URL

Production: https://api.wealthsense.app  
Development: http://localhost:8080

## Authentication

All endpoints except auth require:

- `Authorization: Bearer {jwt_token}`
- `X-Correlation-ID: {uuid}` (optional)

## Rate limiting

100 requests per minute per user. When exceeded, responses use **429 Too Many Requests**. The `X-RateLimit-Remaining` header is included where the gateway enforces limits.

## Idempotency

POST/PUT requests may accept:

- `X-Idempotency-Key: {unique_key}`

Duplicate requests with the same key return the cached response for that operation.

## Services and ports

| Service         | Port | Swagger URL      |
|----------------|------|------------------|
| API Gateway    | 8080 | /swagger-ui.html |
| User Service   | 8081 | /swagger-ui.html |
| Transaction    | 8082 | /swagger-ui.html |
| Fraud Detection| 8083 | /swagger-ui.html |
| Decision Engine| 8084 | /swagger-ui.html |
| Notification   | 8085 | /swagger-ui.html |
| AI Advisor     | 8086 | /swagger-ui.html |
| Investment     | 8087 | /swagger-ui.html |

OpenAPI JSON: `/api-docs` on each service.

## Error codes

| Code                   | Meaning                          |
|------------------------|----------------------------------|
| USER_NOT_FOUND         | User does not exist              |
| INSUFFICIENT_FUNDS     | Balance too low                  |
| FRAUD_DETECTED         | Transaction blocked              |
| RATE_LIMIT_EXCEEDED    | Too many requests                |
| DUPLICATE_TRANSACTION  | Already processed                |
| UNAUTHORIZED           | Invalid or expired token         |

## Endpoints summary

### Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/verify-email`

### Transactions

- `POST /api/v1/transactions`
- `GET /api/v1/transactions`
- `GET /api/v1/transactions/{id}`
- `GET /api/v1/transactions/summary`

### AI advisor

- `POST /api/v1/ai/chat`
- `GET /api/v1/ai/conversations`
- `GET /api/v1/ai/insights`

### Investments

- `POST /api/v1/investments/sip/calculate`
- `GET /api/v1/investments/recommendations`
- `POST /api/v1/investments/goals`
- `GET /api/v1/investments/goals`
- `GET /api/v1/investments/tax-saving`

### API versioning

All public HTTP APIs are under the **`/api/v1`** prefix. Future major versions may introduce `/api/v2` alongside v1; the gateway and ingress route traffic to services that expose these paths.

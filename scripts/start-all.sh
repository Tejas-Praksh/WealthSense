#!/usr/bin/env bash
set -euo pipefail

echo "Starting WealthSense..."

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running. Start Docker Desktop first."
  exit 1
fi

mkdir -p logs

echo "Starting infrastructure..."
docker compose up -d 2>/dev/null || docker-compose up -d

echo "Waiting 30 seconds for infrastructure..."
sleep 30

services=(
  "user-service:8081"
  "transaction-service:8082"
  "fraud-detection-service:8083"
  "decision-engine-service:8084"
  "notification-service:8085"
  "ai-advisor-service:8086"
  "investment-service:8087"
  "api-gateway:8080"
)

for entry in "${services[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  echo "Starting ${name} on :${port}..."
  (
    cd "${ROOT_DIR}/${name}"
    mvn -q spring-boot:run -Dspring-boot.run.profiles=dev \
      >"${ROOT_DIR}/logs/${name}.log" 2>&1 &
  )
  sleep 3
done

echo ""
echo "WealthSense backend processes started (see logs/*.log)."
echo ""
echo "Frontend: http://localhost:3000"
echo "API:      http://localhost:8080"
echo "Swagger:  http://localhost:8081/swagger-ui.html (user-service; each service has its own port)"
echo "Grafana:  http://localhost:3001"
echo "Kibana:   http://localhost:5601"
echo "Zipkin:   http://localhost:9411"

#!/usr/bin/env bash
set -euo pipefail

echo "WealthSense Health Check"
echo "========================="

services=(
  "api-gateway:8080"
  "user-service:8081"
  "transaction-service:8082"
  "fraud-detection-service:8083"
  "decision-engine-service:8084"
  "notification-service:8085"
  "ai-advisor-service:8086"
  "investment-service:8087"
)

PASS=0
FAIL=0

for entry in "${services[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  response="$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/actuator/health" 2>/dev/null || echo "000")"
  if [[ "${response}" == "200" ]]; then
    echo "OK  ${name}"
    PASS=$((PASS + 1))
  else
    echo "FAIL ${name} (HTTP ${response})"
    FAIL=$((FAIL + 1))
  fi
done

echo ""
echo "Passed: ${PASS} | Failed: ${FAIL}"

if [[ "${FAIL}" -eq 0 ]]; then
  echo "All services healthy."
else
  echo "Check failed services and logs/ directory."
fi

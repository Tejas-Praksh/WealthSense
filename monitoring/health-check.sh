#!/bin/bash
# WealthSense Service Health Check Script
# Usage: bash health-check.sh [host]
# Default host: localhost

HOST="${1:-localhost}"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo ""
echo "====================================="
echo "   WealthSense Health Check"
echo "====================================="
echo ""

# Check microservices
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

echo "--- Microservices ---"
all_up=true
for service in "${services[@]}"; do
  name="${service%%:*}"
  port="${service##*:}"
  response=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "http://${HOST}:${port}/actuator/health" 2>/dev/null)
  if [ "$response" == "200" ]; then
    echo -e "${GREEN}✅ $name${NC} (port $port)"
  else
    echo -e "${RED}❌ $name${NC} (port $port) [HTTP $response]"
    all_up=false
  fi
done

echo ""
echo "--- Monitoring Stack ---"
monitoring=(
  "Prometheus:9090"
  "Grafana:3001"
  "Kibana:5601"
  "Zipkin:9411"
  "Elasticsearch:9200"
)

for svc in "${monitoring[@]}"; do
  name="${svc%%:*}"
  port="${svc##*:}"
  response=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "http://${HOST}:${port}" 2>/dev/null)
  if [ "$response" == "200" ] || [ "$response" == "302" ]; then
    echo -e "${GREEN}✅ $name${NC} → http://${HOST}:${port}"
  else
    echo -e "${YELLOW}⚠️  $name${NC} → http://${HOST}:${port} [HTTP $response]"
  fi
done

echo ""
echo "====================================="
if [ "$all_up" = true ]; then
  echo -e "${GREEN}All microservices are healthy! 🎉${NC}"
else
  echo -e "${RED}Some services are DOWN. Check logs.${NC}"
fi
echo "====================================="
echo ""

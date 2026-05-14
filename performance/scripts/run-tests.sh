#!/usr/bin/env bash
set -euo pipefail

# Usage: bash scripts/run-tests.sh 'https://gateway.example.com' ['https://fraud-service.example.com']

DEFAULT_URL="${DEFAULT_GATEWAY_URL:-https://wealthsense-api.onrender.com}"
BASE_URL="${1:-$DEFAULT_URL}"
FRAUD_URL_RAW="${2:-${FRAUD_SERVICE_PUBLIC_URL:-}}"

parse_url() {
  local raw="$1"
  local proto rest hostport host port
  if [[ "$raw" == https://* ]]; then
    proto=https
    rest="${raw#https://}"
  elif [[ "$raw" == http://* ]]; then
    proto=http
    rest="${raw#http://}"
  else
    proto=https
    rest="$raw"
  fi
  hostport="${rest%%/*}"
  host="${hostport%%:*}"
  if [[ "$hostport" == *:* ]]; then
    port="${hostport##*:}"
  else
    port=""
  fi
  if [[ -z "$port" ]]; then
    if [[ "$proto" == "https" ]]; then port=443; else port=80; fi
  fi
  printf '%s %s %s' "$proto" "$host" "$port"
}

read -r PROTO HOST PORT <<<"$(parse_url "$BASE_URL")"

RESULTS_DIR="./results/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$RESULTS_DIR"

echo "Running WealthSense performance tests..."
echo "Gateway: ${PROTO}://${HOST}:${PORT}"
echo "Results: ${RESULTS_DIR}"

COMMON_JMETER=(jmeter -n -Jprotocol="$PROTO" -Jhost="$HOST" -Jport="$PORT" -Jramp=60 -Jduration=300)

echo "Running auth load test..."
"${COMMON_JMETER[@]}" \
  -t test-plans/auth-load-test.jmx \
  -l "$RESULTS_DIR/auth-results.jtl" \
  -Jthreads=100

echo "Running transaction load test..."
"${COMMON_JMETER[@]}" \
  -t test-plans/transaction-load-test.jmx \
  -l "$RESULTS_DIR/transaction-results.jtl" \
  -Jthreads=50

if [[ -n "${FRAUD_URL_RAW}" ]]; then
  read -r FPROTO FHOST FPORT <<<"$(parse_url "$FRAUD_URL_RAW")"
  echo "Running fraud service health probe (${FPROTO}://${FHOST}:${FPORT})..."
  "${COMMON_JMETER[@]}" \
    -t test-plans/fraud-detection-test.jmx \
    -l "$RESULTS_DIR/fraud-results.jtl" \
    -Jfraud_protocol="$FPROTO" -Jfraud_host="$FHOST" -Jfraud_port="$FPORT" \
    -Jthreads=20
else
  echo "Skipping fraud-detection-test.jmx (set arg 2 or FRAUD_SERVICE_PUBLIC_URL to a reachable fraud service base URL)."
fi

echo "Running AI advisor test..."
"${COMMON_JMETER[@]}" \
  -t test-plans/ai-advisor-test.jmx \
  -l "$RESULTS_DIR/ai-advisor-results.jtl" \
  -Jthreads=10

echo "Tests complete!"
echo "Results in: $RESULTS_DIR"

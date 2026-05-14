#!/usr/bin/env bash
set -euo pipefail

# Usage: bash measure-latency.sh 'https://gateway.example.com'
# Optional: LATENCY_EMAIL + LATENCY_PASSWORD to measure login.
# Optional: LATENCY_TOKEN (Bearer value) for authenticated endpoints (SIP).

BASE_URL="${1:-http://localhost:8080}"

to_ms() {
  awk -v x="$1" 'BEGIN { printf "%.0f\n", x * 1000 }'
}

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
    proto=http
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
BASE="${PROTO}://${HOST}:${PORT}"

echo "Measuring WealthSense API latency (10 samples each)"
echo "Target: $BASE"
echo "Requires: curl, awk. Optional: python3 for login/SIP token flows."
echo "=========================================="

measure_latency() {
  local name="$1"
  local path="$2"
  local method="${3:-GET}"
  local data="${4:-}"
  local extra=()
  if (($# > 4)); then
    extra=("${@:5}")
  fi

  local total=0 min=9999999 max=0
  local i
  for ((i = 1; i <= 10; i++)); do
    local latency
    if [[ -n "$data" ]]; then
      latency=$(curl -s -o /dev/null -w "%{time_total}" -X "$method" \
        -H "Content-Type: application/json" \
        "${extra[@]}" \
        -d "$data" \
        "$BASE$path")
    else
      latency=$(curl -s -o /dev/null -w "%{time_total}" -X "$method" "${extra[@]}" "$BASE$path")
    fi
    local ms
    ms=$(to_ms "$latency")
    total=$((total + ms))
    if (( ms < min )); then min=$ms; fi
    if (( ms > max )); then max=$ms; fi
  done
  local avg=$((total / 10))
  echo "$name"
  echo "  Avg: ${avg}ms | Min: ${min}ms | Max: ${max}ms"
}

measure_latency "Health" "/actuator/health"

if [[ -n "${LATENCY_EMAIL:-}" && -n "${LATENCY_PASSWORD:-}" ]]; then
  LOGIN_JSON=$(printf '%s' "{\"email\":\"${LATENCY_EMAIL}\",\"password\":\"${LATENCY_PASSWORD}\"}")
  measure_latency "Login" "/api/v1/auth/login" "POST" "$LOGIN_JSON"
fi

if [[ -n "${LATENCY_TOKEN:-}" ]]; then
  AUTH=( -H "Authorization: Bearer ${LATENCY_TOKEN}" )
  SIP_JSON='{"monthlyAmount":1000,"years":10,"expectedReturn":12}'
  measure_latency "SIP calculate" "/api/v1/investments/sip/calculate" "POST" "$SIP_JSON" "${AUTH[@]}"
fi

echo ""
echo "Paste these lines into performance/RESULTS.md after a successful production run."

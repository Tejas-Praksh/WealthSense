#!/usr/bin/env bash
set -euo pipefail

# Smoke-test public and authenticated routes through the API Gateway.
# Usage: bash api-tests/test-all-endpoints.sh 'https://gateway.example.com'
#
# Requires: curl, mktemp. Token parsing: python3 (preferred) or python.

BASE_URL="${1:-http://localhost:8080}"

PASS=0
FAIL=0
START_TIME=$(date +%s)

json_access_token() {
  if command -v python3 >/dev/null 2>&1; then
    python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('accessToken') or '')" 2>/dev/null
  elif command -v python >/dev/null 2>&1; then
    python -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('accessToken') or '')" 2>/dev/null
  else
    echo ""
    return 1
  fi
}

echo "Testing WealthSense APIs"
echo "Base URL: $BASE_URL"
echo "================================"

test_endpoint() {
  local name=$1
  local method=$2
  local url=$3
  local data=$4
  local expected=$5
  shift 5
  local extra=("$@")
  local tmp
  tmp=$(mktemp)
  local code
  if [[ -n "$data" ]]; then
    code=$(curl -sS -o "$tmp" -w "%{http_code}" -X "$method" \
      -H "Content-Type: application/json" \
      "${extra[@]}" \
      -d "$data" \
      "$BASE_URL$url")
  else
    code=$(curl -sS -o "$tmp" -w "%{http_code}" -X "$method" \
      "${extra[@]}" \
      "$BASE_URL$url")
  fi

  if [[ "$code" == "$expected" ]]; then
    echo "OK  $name ($code)"
    PASS=$((PASS + 1))
  else
    echo "FAIL $name (expected $expected, got $code)"
    cat "$tmp" || true
    FAIL=$((FAIL + 1))
  fi
  rm -f "$tmp"
}

echo ""
echo "Health:"
test_endpoint "API Gateway actuator health" GET "/actuator/health" "" "200"

echo ""
echo "Auth:"
TIMESTAMP=$(date +%s)
TEST_EMAIL="test_${TIMESTAMP}@example.com"

REGISTER_JSON=$(cat <<EOF
{"email":"${TEST_EMAIL}","password":"Password@123","firstName":"Test","lastName":"User","phone":"9876543210"}
EOF
)

test_endpoint "Register user" POST "/api/v1/auth/register" "$REGISTER_JSON" "201"

LOGIN_JSON=$(printf '%s' "{\"email\":\"${TEST_EMAIL}\",\"password\":\"Password@123\"}")
test_endpoint "Login user" POST "/api/v1/auth/login" "$LOGIN_JSON" "200"

TOKEN=$(curl -sS -X POST -H "Content-Type: application/json" -d "$LOGIN_JSON" "$BASE_URL/api/v1/auth/login" | json_access_token || true)

if [[ -z "${TOKEN:-}" ]]; then
  echo "Could not parse accessToken (install python3). Skipping authenticated checks."
else
  AUTH=( -H "Authorization: Bearer ${TOKEN}" )

  echo ""
  echo "User profile:"
  test_endpoint "Get profile" GET "/api/v1/users/profile" "" "200" "${AUTH[@]}"

  echo ""
  echo "Transactions:"
  test_endpoint "Get transactions" GET "/api/v1/transactions?page=0&size=20" "" "200" "${AUTH[@]}"

  echo ""
  echo "Investments:"
  SIP_JSON='{"monthlyAmount":1000,"years":10,"expectedReturn":12}'
  test_endpoint "SIP calculate" POST "/api/v1/investments/sip/calculate" "$SIP_JSON" "200" "${AUTH[@]}"

  test_endpoint "Get goals" GET "/api/v1/investments/goals" "" "200" "${AUTH[@]}"

  echo ""
  echo "AI:"
  test_endpoint "Get AI insights" GET "/api/v1/ai/insights" "" "200" "${AUTH[@]}"
fi

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "================================"
echo "Results: passed=$PASS failed=$FAIL duration=${DURATION}s"
echo "================================"

if [[ "$FAIL" -eq 0 ]]; then
  echo "All executed checks passed."
  exit 0
else
  echo "Some checks failed."
  exit 1
fi

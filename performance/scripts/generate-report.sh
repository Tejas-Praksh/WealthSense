#!/usr/bin/env bash
set -euo pipefail

# Usage: bash scripts/generate-report.sh results/20260101_120000

if [[ -z "${1:-}" ]] || [[ ! -d "$1" ]]; then
  echo "Usage: bash scripts/generate-report.sh <results-directory>" >&2
  exit 1
fi

RESULTS_DIR="$1"

gen_one() {
  local jtl="$1"
  local out="$2"
  if [[ -f "$jtl" ]]; then
    jmeter -g "$jtl" -o "$out"
    echo "Generated: $out/index.html"
  else
    echo "Skip (missing JTL): $jtl"
  fi
}

gen_one "$RESULTS_DIR/auth-results.jtl" "$RESULTS_DIR/auth-report"
gen_one "$RESULTS_DIR/transaction-results.jtl" "$RESULTS_DIR/transaction-report"
gen_one "$RESULTS_DIR/fraud-results.jtl" "$RESULTS_DIR/fraud-report"
gen_one "$RESULTS_DIR/ai-advisor-results.jtl" "$RESULTS_DIR/ai-advisor-report"

echo "Done. Open the generated index.html files under: $RESULTS_DIR"

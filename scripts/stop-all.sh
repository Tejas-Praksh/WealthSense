#!/usr/bin/env bash
set -euo pipefail

echo "Stopping WealthSense..."

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if command -v pkill >/dev/null 2>&1; then
  pkill -f "spring-boot:run" 2>/dev/null || true
else
  echo "pkill not found; stop Java processes manually if needed."
fi

docker compose down 2>/dev/null || docker-compose down 2>/dev/null || true

echo "All stopped."

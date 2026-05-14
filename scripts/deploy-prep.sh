#!/usr/bin/env bash
set -euo pipefail

echo "Preparing WealthSense for deployment..."

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Building all services..."
mvn clean package -DskipTests

echo "Building frontend..."
cd wealthsense-frontend
npm ci
npm run build
cd ..

echo "Build complete! Ready to deploy."

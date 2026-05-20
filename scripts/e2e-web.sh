#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE="-f $REPO_ROOT/api/docker-compose.yml -f $REPO_ROOT/apps/web/docker-compose.yml"

teardown() {
  echo "--- tearing down stack ---"
  docker compose $COMPOSE down --volumes
}
trap teardown EXIT

echo "--- building images ---"
docker build -t ssv-api:latest -f api/Dockerfile api
docker build -t ssv-web:latest -f apps/web/Dockerfile .

echo "--- starting stack ---"
API_IMAGE=ssv-api:latest \
WEB_IMAGE=ssv-web:latest \
docker compose $COMPOSE up -d --wait

echo "--- running Cypress ---"
cd apps/web
pnpm install --frozen-lockfile
pnpm test:e2e

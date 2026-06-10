#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE="-f $REPO_ROOT/api/docker-compose.yml -f $REPO_ROOT/apps/web/docker-compose.yml"

teardown() {
  echo "--- tearing down stack ---"
  docker compose $COMPOSE down --volumes
}
trap teardown EXIT

# The web image bakes its Vite config at build time. The build context excludes
# .env files (see .dockerignore), so the values must be passed as build args
# — sourced here from the local .env.local, the same file Cypress reads.
WEB_ENV="$REPO_ROOT/apps/web/.env.local"
if [ ! -f "$WEB_ENV" ]; then
  echo "error: $WEB_ENV not found — copy apps/web/.env.sample and fill in Auth0 values" >&2
  exit 1
fi
set -a
# shellcheck disable=SC1090
. "$WEB_ENV"
set +a

echo "--- building images ---"
docker build -t ssv-api:latest -f api/Dockerfile api
docker build -t ssv-web:latest -f apps/web/Dockerfile \
  --build-arg VITE_AUTH0_DOMAIN="$VITE_AUTH0_DOMAIN" \
  --build-arg VITE_AUTH0_CLIENT_ID="$VITE_AUTH0_CLIENT_ID" \
  --build-arg VITE_AUTH0_AUDIENCE="$VITE_AUTH0_AUDIENCE" \
  --build-arg VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:8080}" \
  .

echo "--- starting stack ---"
API_IMAGE=ssv-api:latest \
WEB_IMAGE=ssv-web:latest \
docker compose $COMPOSE up -d --wait

echo "--- running Cypress ---"
cd apps/web
pnpm install --frozen-lockfile
pnpm test:e2e

#!/usr/bin/env bash
# Shared helpers for the load/stress run scripts. SOURCED, not executed.
# Keeps boot/seed/venv setup in one place so the two entry scripts stay thin.

set -euo pipefail

# tests/load/ (this lib's dir) and the repo root above it.
LOAD_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${LOAD_DIR}/../.." && pwd)"

# The two compose files layered to boot the API under the loadtest-nojwt profile
# (resource server OFF; investor identity arrives in the X-Loadtest-Subject header).
COMPOSE=(docker compose
	-f "${REPO_ROOT}/api/docker-compose.local.yml"
	-f "${LOAD_DIR}/docker-compose.loadtest-nojwt.yml")

log() { printf '\n=== %s ===\n' "$*"; }

boot_stack() {
	log "Booting API + Postgres under the loadtest-nojwt profile"
	"${COMPOSE[@]}" up --build -d
}

wait_for_health() {
	log "Waiting for API health"
	local tries=60
	until curl -fs http://localhost:8080/actuator/health >/dev/null 2>&1; do
		tries=$((tries - 1))
		if [ "${tries}" -le 0 ]; then
			echo "API did not become healthy in time" >&2
			exit 1
		fi
		sleep 2
	done
	echo "API is up."
}

seed_companies() {
	# companies.sql is idempotent, so re-running between scenarios is safe.
	log "Seeding companies table"
	"${COMPOSE[@]}" exec -T db \
		psql -U ssv -d ssv < "${LOAD_DIR}/seed/companies.sql"
}

ensure_venv() {
	log "Preparing Locust client venv"
	cd "${LOAD_DIR}"
	if [ ! -d .venv ]; then
		python -m venv .venv
		# shellcheck disable=SC1091
		. .venv/bin/activate
		pip install -q -r requirements.txt
	else
		# shellcheck disable=SC1091
		. .venv/bin/activate
	fi
	mkdir -p results
}

prepare_stack() {
	boot_stack
	wait_for_health
	seed_companies
	ensure_venv
}

run_scenario() {
	# $1 = shape (normal|stress), $2 = output prefix, $3.. = extra locust args.
	local shape="$1"; shift
	local prefix="$1"; shift
	log "Running '${shape}' scenario -> tests/load/results/${prefix}.*"
	cd "${LOAD_DIR}"
	# loadtest-nojwt disables the resource server, so the client authenticates by
	# sending the investor subject in X-Loadtest-Subject instead of minting a JWT.
	LOCUST_SHAPE="${shape}" LOADTEST_AUTH_MODE=header locust -f locustfile.py --headless \
		--host http://localhost:8080 \
		--csv "results/${prefix}" --html "results/${prefix}.html" "$@"
	log "Done. Artifacts in tests/load/results/${prefix}*"
	echo "Tear down (wipes synthetic DB data) with:"
	echo "  ${COMPOSE[*]} down -v"
}

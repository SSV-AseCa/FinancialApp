#!/usr/bin/env bash
# One-shot NORMAL load run: boot the loadtest stack, seed, then drive the steady
# plateau scenario (LOADTEST_NORMAL_USERS for LOADTEST_NORMAL_HOLD_SECONDS).
#
# Any extra args are passed straight to locust, e.g. a clean DB-only run:
#   ./run-normal-load.sh PortfolioCoreUser WatchlistValueUser
set -euo pipefail

# shellcheck source=_loadtest-lib.sh
. "$(dirname "${BASH_SOURCE[0]}")/_loadtest-lib.sh"

prepare_stack
run_scenario normal normal "$@"

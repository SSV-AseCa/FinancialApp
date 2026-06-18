#!/usr/bin/env bash
# One-shot STRESS run: boot the loadtest stack, seed, then drive the stepwise ramp
# to saturation (LOADTEST_STRESS_STEPS held LOADTEST_STRESS_STEP_SECONDS each).
#
# Any extra args are passed straight to locust, e.g. an isolated EDGAR probe:
#   ./run-stress-load.sh CompaniesUser
set -euo pipefail

# shellcheck source=_loadtest-lib.sh
. "$(dirname "${BASH_SOURCE[0]}")/_loadtest-lib.sh"

prepare_stack
run_scenario stress stress "$@"

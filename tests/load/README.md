# SSV API — Load & Stress Testing (Locust)

Locust scenarios that exercise the SSV API. These run **on demand** (not in CI, per
`vault/CI.md`) against the Docker-deployed stack. The vault requires at minimum a
**normal-load** and a **peak/stress** scenario, with results documented before the
final presentation (`vault/Delivery and Quality Constraints.md`).

## How authentication works here

The API is an OAuth2 resource server — every route except `GET /actuator/health`
needs a Bearer JWT, and the API never mints tokens. To test offline, we run the API
under the **`loadtest` Spring profile**, which swaps the Auth0 decoder for a local
HS256 decoder (`LoadTestJwtDecoderConfig`). Locust signs its own tokens with a
shared secret (`common/auth.py`); each distinct `sub` becomes a distinct investor
(lazy provisioning), so we simulate many investors with one secret.

> ⚠️ The `loadtest` profile trusts any token signed with the shared secret. It is a
> local/CI-on-demand convenience and **must never be the active profile in a
> released image.**

## Layout

```
locustfile.py                  Entry point; registers users, selects shape via env
locust.conf                    Shared defaults (host, locustfile)
docker-compose.loadtest.yml    Additive override: boots the API in `loadtest` profile
common/config.py               Env-driven config (must mirror the loadtest profile)
common/auth.py                 Local HS256 token minting (PyJWT)
users/portfolio_core_user.py   DB-backed portfolio/transactions/positions (weight 8)
users/watchlist_value_user.py  Watchlist + portfolio value                (weight 3)
users/companies_user.py        EDGAR-backed search/history/metrics         (weight 1)
shapes/normal_load.py          Steady plateau (normal scenario)
shapes/stress_load.py          Stepwise ramp to saturation (stress scenario)
seed/companies.sql             Seeds the `companies` table (watchlist/metrics need it)
```

## Prerequisites

- Docker + Docker Compose
- Python 3.11+
- Outbound network + valid `EDGAR_USER_AGENT` only if you run the EDGAR/Yahoo paths
  (`CompaniesUser`, non-zero `/portfolio/value`). The DB-backed scenarios need
  neither.

## 1. Start the stack under the loadtest profile

From the repo root:

```bash
docker compose \
  -f api/docker-compose.local.yml \
  -f tests/load/docker-compose.loadtest.yml \
  up --build -d

# wait for health
curl -fs http://localhost:8080/actuator/health
```

To run the EDGAR/Yahoo paths, also export `EDGAR_USER_AGENT` (and any API keys)
before `up` so they reach the API container.

## 2. Seed companies (needed for watchlist + metrics)

```bash
docker compose -f api/docker-compose.local.yml exec -T db \
  psql -U ssv -d ssv < tests/load/seed/companies.sql
```

## 3. Install the Locust client

```bash
cd tests/load
python -m venv .venv && . .venv/bin/activate
pip install -r requirements.txt
```

## 4. Run the scenarios (headless, with CSV output)

The token defaults in `common/config.py` already match
`docker-compose.loadtest.yml`. If you override `LOADTEST_JWT_SECRET` on the API
side, export the **same** value before running Locust.

```bash
mkdir -p results
```

**Normal load** — steady plateau (default 20 users for 5 min):

```bash
LOCUST_SHAPE=normal locust -f locustfile.py --headless \
  --host http://localhost:8080 \
  --csv results/normal --html results/normal.html
```

**Peak / stress load** — stepwise ramp 25→50→100→200→400 (60 s/step):

```bash
LOCUST_SHAPE=stress locust -f locustfile.py --headless \
  --host http://localhost:8080 \
  --csv results/stress --html results/stress.html
```

**Clean DB-only run** (exclude the EDGAR class for an uncontaminated app+DB signal):

```bash
LOCUST_SHAPE=normal locust -f locustfile.py --headless \
  --host http://localhost:8080 \
  --csv results/db_only PortfolioCoreUser WatchlistValueUser
```

**Isolated EDGAR rate-limit probe** (verify the 10 req/s ceiling holds):

```bash
locust -f locustfile.py --headless -u 50 -r 10 -t 2m \
  --host http://localhost:8080 \
  --csv results/edgar CompaniesUser
```

Drop `--headless` (and the `-u/-r/-t`) to use the Locust web UI at
http://localhost:8089 instead.

## Tunables (env vars)

| Variable | Default | Effect |
|---|---|---|
| `LOCUST_SHAPE` | _(unset)_ | `normal` / `stress`; unset → CLI-driven `-u/-r/-t` |
| `LOADTEST_NORMAL_USERS` | 20 | Normal plateau user count |
| `LOADTEST_NORMAL_HOLD_SECONDS` | 300 | Normal plateau duration |
| `LOADTEST_STRESS_STEPS` | `25,50,100,200,400` | Stress step user counts |
| `LOADTEST_STRESS_STEP_SECONDS` | 60 | Seconds held per stress step |
| `LOADTEST_JWT_SECRET` / `_ISSUER` / `_AUDIENCE` | see config | Must match the API side |

## Reading the results

- **DB-backed endpoints** are the clean perf signal. Failure rate should stay near
  zero (4xx domain responses like 409/422 are marked as expected and excluded).
- **EDGAR endpoints** block behind a 10 req/s limiter, so under stress they show up
  as **latency**, not errors. 503/404/400 there are expected and excluded from the
  failure rate.
- Record numbers in `vault/Load and Stress Testing Results.md`.

## Tear down

```bash
docker compose -f api/docker-compose.local.yml -f tests/load/docker-compose.loadtest.yml down -v
```

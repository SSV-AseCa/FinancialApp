# Load and Stress Testing Results

Status: **template — awaiting an executed run.** The suite lives in `tests/load/`;
see its `README.md` for how to run. This document records the **strategy, its
justification, and the interpreted results**, as required by
[[Delivery and Quality Constraints]] and [[Tech Stack]].

---

## 1. Strategy and justification

**Tool:** Locust (mandated by [[requirements/Constraints]]). **Scenarios:** a
normal-load plateau and a peak/stress stepwise ramp (mandated minimum).

**Authentication under test.** The API is an Auth0 OAuth2 resource server and mints
no tokens, which makes it impossible to load-test through Auth0 offline. We run the
API under a dedicated **`loadtest` Spring profile** that swaps in a local HS256
`JwtDecoder`; Locust signs its own tokens, one `sub` per virtual user, so each VU is
an isolated investor (lazy provisioning by `sub`). This is profile-gated and never
shipped — see the warning in `tests/load/README.md`.

**Endpoint classification** (drives how results are read):

| Class | Endpoints | Backing | Expectation |
|---|---|---|---|
| DB-backed | `/portfolio`, `/portfolio/transactions/*`, `/portfolio/positions/*`, `/portfolio/value`, `/watchlist` | Postgres only | Clean perf signal; near-zero failures |
| External | `/companies/search\|history\|metrics` | SEC EDGAR (blocking 10 req/s limiter) | Degrades as **latency**, not errors |

Domain responses that are *correct* under repeated load are marked expected and
**excluded from the failure rate**: 409 (duplicate watchlist), 422 (transaction
rule), 404 (unknown CIK/position), 400 (validation), 503 (EDGAR throttle). So a
non-zero failure rate means genuine 5xx/timeouts.

**Pass/fail thresholds (self-defined — the repo documents none).** Justification:
there are no numeric SLAs anywhere in the vault; the only objective, repo-backed
target is the EDGAR ceiling. We therefore set pragmatic baselines and otherwise
*characterize* behavior rather than assert arbitrary numbers.

| Target | Threshold | Why |
|---|---|---|
| DB-backed, normal load | p95 < 300 ms, failures < 1% | Reasonable interactive-API baseline; revisit with real SLAs |
| Stress | Report the **saturation knee** (where p95 degrades sharply or errors appear) | No SLA → characterize capacity, don't pass/fail |
| EDGAR outbound | **≤ 10 req/s under any inbound load** | Objective, mandated by [[stories/Enforce EDGAR Rate Limit]] (`edgar.rate-limit.*`) |

---

## 2. Environment

Fill in at run time.

- Date / commit:
- Host running the stack (CPU / RAM):
- API + DB: Docker Compose (`api/docker-compose.local.yml` + `tests/load/docker-compose.loadtest.yml`), profile `loadtest`
- Locust version / VU host:
- Notes (EDGAR/Yahoo reachable? keys set?):

---

## 3. Normal-load scenario

Shape: `NormalLoad` — _N_ users, _M_-minute plateau. Command: see README §4.

| Endpoint (name) | # Requests | Failures | p50 (ms) | p95 (ms) | p99 (ms) | RPS |
|---|---|---|---|---|---|---|
| GET /portfolio | | | | | | |
| POST /portfolio/transactions/buy | | | | | | |
| POST /portfolio/transactions/sell | | | | | | |
| GET /portfolio/transactions | | | | | | |
| POST /portfolio/positions | | | | | | |
| GET /portfolio/value | | | | | | |
| POST /watchlist | | | | | | |
| **Aggregate** | | | | | | |

Result vs threshold (p95 < 300 ms, failures < 1%): _PASS / FAIL_ — comment:

---

## 4. Peak / stress scenario

Shape: `StressLoad` — steps `25,50,100,200,400`, 60 s each. Command: see README §4.

| Step (users) | RPS | p95 (ms) | Failure % | Observation |
|---|---|---|---|---|
| 25 | | | | |
| 50 | | | | |
| 100 | | | | |
| 200 | | | | |
| 400 | | | | |

**Saturation knee:** _at ~___ users, p95 jumps from ___ ms to ___ ms / errors begin._
**Bottleneck hypothesis:** _(DB pool / CPU / connection limits / GC — back with evidence)._

---

## 5. EDGAR rate-limit probe

Command: isolated `CompaniesUser` run (README §4). Verify the outbound SEC rate.

- Inbound RPS driven at the API:
- Observed EDGAR outbound rate (from API logs / limiter): _≤ 10 req/s?_ **PASS / FAIL**
- Manifestation under load (latency vs 503s):

---

## 6. Interpretation & follow-ups

- Summary of whether the system meets the (self-defined) baselines:
- Capacity headroom and the knee:
- Recommended next steps (tuning, real SLAs, re-run conditions):

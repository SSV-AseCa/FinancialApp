"""Central, env-driven configuration for the SSV load-test suite.

Every value has a default tuned for a local Docker stack running the API under the
`loadtest` Spring profile. The JWT secret/issuer/audience MUST match
api/src/main/resources/application-loadtest.properties (override both sides with
the same env vars for a non-default run).
"""

import os


def _env(name, default):
	value = os.getenv(name)
	return value if value not in (None, "") else default


# --- Target -----------------------------------------------------------------
# Locust's --host flag overrides this; kept here for documentation/scripts.
HOST = _env("LOCUST_HOST", "http://localhost:8080")

# --- Local JWT signing (must mirror the loadtest Spring profile) ------------
JWT_SECRET = _env("LOADTEST_JWT_SECRET",
		"ssv-loadtest-shared-secret-change-me-please-0123456789")
JWT_ISSUER = _env("LOADTEST_JWT_ISSUER", "https://loadtest.ssv.local/")
JWT_AUDIENCE = _env("LOADTEST_JWT_AUDIENCE", "ssv-loadtest")
JWT_ALGORITHM = "HS256"
# Token lifetime in seconds; long enough to outlast a stress run.
JWT_TTL_SECONDS = int(_env("LOADTEST_JWT_TTL_SECONDS", "7200"))

# --- Test data --------------------------------------------------------------
# CIKs seeded by tests/load/seed/companies.sql -- safe for watchlist / metrics.
SEEDED_CIKS = [
	"0000320193",  # AAPL
	"0000789019",  # MSFT
	"0001652044",  # GOOGL
	"0001018724",  # AMZN
	"0001045810",  # NVDA
	"0001326801",  # META
	"0001318605",  # TSLA
	"0000019617",  # JPM
]

# Tickers the Yahoo scheduler fetches by default -> make /portfolio/value
# non-zero when used as position tickers.
PRICED_TICKERS = ["AAPL", "MSFT", "GOOGL"]

# Free-text terms for /companies/search (EDGAR full-text search).
SEARCH_TERMS = ["apple", "microsoft", "tesla", "nvidia", "amazon", "bank"]

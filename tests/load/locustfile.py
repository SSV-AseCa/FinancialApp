"""Entry point for the SSV API load/stress suite.

User classes (always registered):
    PortfolioCoreUser   -- DB-backed portfolio/transactions/positions (weight 8)
    WatchlistValueUser  -- watchlist + portfolio value                (weight 3)
    CompaniesUser       -- EDGAR-backed search/history/metrics         (weight 1)

Restrict to a subset on the CLI by naming classes, e.g. a clean DB-only run:
    locust -f locustfile.py PortfolioCoreUser WatchlistValueUser

Scenario selection via the LOCUST_SHAPE env var:
    LOCUST_SHAPE=normal   -> NormalLoad  (steady plateau)
    LOCUST_SHAPE=stress   -> StressLoad  (stepwise ramp to saturation)
    (unset)               -> no shape; you control -u/-r/-t from the CLI
"""

import os

# Importing the user classes registers them with Locust.
from users.portfolio_core_user import PortfolioCoreUser  # noqa: F401
from users.watchlist_value_user import WatchlistValueUser  # noqa: F401
from users.companies_user import CompaniesUser  # noqa: F401

# Locust discovers exactly one LoadTestShape subclass in this module's namespace,
# so we conditionally import only the selected one.
_shape = os.getenv("LOCUST_SHAPE", "").strip().lower()
if _shape == "normal":
	from shapes.normal_load import NormalLoad as LoadShape  # noqa: F401
elif _shape == "stress":
	from shapes.stress_load import StressLoad as LoadShape  # noqa: F401

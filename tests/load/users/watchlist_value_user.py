"""Watchlist + portfolio-value user.

POST /watchlist requires the company to exist (seed tests/load/seed/companies.sql
first). Watchlist entries are unique per (investor, company), so repeated adds of
the same company return 409 -- an EXPECTED domain response under load, not a
failure. GET /portfolio/value is a cheap DB read (positions x latest market price).
"""

import random

from locust import HttpUser, between, task

from common import config
from common.auth import auth_header_for_new_user


class WatchlistValueUser(HttpUser):

	weight = 3
	wait_time = between(1, 3)

	def on_start(self):
		self.client.headers.update(auth_header_for_new_user())

	@task(3)
	def add_to_watchlist(self):
		cik = random.choice(config.SEEDED_CIKS)
		with self.client.post("/watchlist",
				json={"cik": cik},
				name="POST /watchlist",
				catch_response=True) as response:
			# 409 = already watched, 400 = unknown/invalid CIK -- both expected.
			if response.status_code in (201, 400, 409):
				response.success()

	@task(2)
	def portfolio_value(self):
		self.client.get("/portfolio/value", name="GET /portfolio/value")

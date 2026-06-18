"""Portfolio / transactions / positions user -- the pure-Postgres workhorse.

Every endpoint here is DB-backed (no external EDGAR/Yahoo calls), so this class
is safe to drive at high RPS and gives the cleanest signal for the app+Postgres
tier. `buy` accepts arbitrary CIK strings, so no seeding is required.
"""

import datetime
import random

from locust import HttpUser, between, task

from common.auth import auth_header_for_new_user


class PortfolioCoreUser(HttpUser):

	weight = 8
	wait_time = between(1, 3)

	def on_start(self):
		# One token == one investor for this user's whole lifetime.
		self.client.headers.update(auth_header_for_new_user())
		self.owned_ciks = []
		self.position_ids = []
		# Trigger lazy investor+portfolio provisioning up front.
		self.client.get("/portfolio", name="GET /portfolio (provision)")

	@staticmethod
	def _random_cik():
		return "{0:010d}".format(random.randint(1, 9_999_999_999))

	@task(5)
	def buy(self):
		cik = self._random_cik()
		response = self.client.post("/portfolio/transactions/buy",
				json={"cik": cik, "quantity": random.randint(1, 100)},
				name="POST /portfolio/transactions/buy")
		if response.status_code == 201:
			self.owned_ciks.append(cik)

	@task(2)
	def sell(self):
		if not self.owned_ciks:
			return
		cik = random.choice(self.owned_ciks)
		with self.client.post("/portfolio/transactions/sell",
				json={"cik": cik, "quantity": 1},
				name="POST /portfolio/transactions/sell",
				catch_response=True) as response:
			# 422 == domain rule (e.g. oversold) -- expected, not a load failure.
			if response.status_code in (201, 422):
				response.success()

	@task(3)
	def view_portfolio(self):
		self.client.get("/portfolio", name="GET /portfolio")

	@task(2)
	def transaction_history(self):
		self.client.get("/portfolio/transactions", name="GET /portfolio/transactions")

	@task(2)
	def add_position(self):
		ticker = random.choice(["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"])
		response = self.client.post("/portfolio/positions",
				json={
					"ticker": ticker,
					"quantity": random.randint(1, 50),
					"operationDate": datetime.date.today().isoformat(),
				},
				name="POST /portfolio/positions")
		if response.status_code == 201:
			self.position_ids.append(response.json()["id"])

	@task(1)
	def update_position(self):
		if not self.position_ids:
			return
		position_id = random.choice(self.position_ids)
		with self.client.put("/portfolio/positions/{0}".format(position_id),
				json={
					"ticker": random.choice(["AAPL", "MSFT", "GOOGL"]),
					"quantity": random.randint(1, 50),
					"operationDate": datetime.date.today().isoformat(),
				},
				name="PUT /portfolio/positions/{id}",
				catch_response=True) as response:
			if response.status_code in (200, 404):
				response.success()

	@task(1)
	def delete_position(self):
		if not self.position_ids:
			return
		position_id = self.position_ids.pop()
		with self.client.delete("/portfolio/positions/{0}".format(position_id),
				name="DELETE /portfolio/positions/{id}",
				catch_response=True) as response:
			if response.status_code in (204, 404):
				response.success()

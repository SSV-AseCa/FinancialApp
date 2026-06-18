"""Companies user -- exercises the EDGAR-backed endpoints.

/companies/search|history|metrics call SEC EDGAR behind a *blocking* 10 req/s
limiter. Under stress this surfaces as latency (queued behind the limiter) rather
than errors. 503 (EdgarRateLimitException), 404 (unknown CIK) and 400 are EXPECTED
domain/throttle responses here and are not counted as load failures, so the
reported failure rate reflects only genuine 5xx/timeouts.

Kept in its own class (low weight) so it can be run in isolation:
    locust -f locustfile.py CompaniesUser
"""

import random

from locust import HttpUser, between, task

from common import config
from common.auth import auth_header_for_new_user

_EXPECTED = (200, 400, 404, 503)


class CompaniesUser(HttpUser):

	weight = 1
	wait_time = between(1, 3)

	def on_start(self):
		self.client.headers.update(auth_header_for_new_user())

	@task(3)
	def search(self):
		term = random.choice(config.SEARCH_TERMS)
		with self.client.get("/companies/search",
				params={"q": term},
				name="GET /companies/search",
				catch_response=True) as response:
			if response.status_code in _EXPECTED:
				response.success()

	@task(2)
	def history(self):
		cik = random.choice(config.SEEDED_CIKS)
		with self.client.get("/companies/{0}/history".format(cik),
				name="GET /companies/{cik}/history",
				catch_response=True) as response:
			if response.status_code in _EXPECTED:
				response.success()

	@task(2)
	def metrics(self):
		cik = random.choice(config.SEEDED_CIKS)
		with self.client.get("/companies/{0}/metrics".format(cik),
				name="GET /companies/{cik}/metrics",
				catch_response=True) as response:
			if response.status_code in _EXPECTED:
				response.success()

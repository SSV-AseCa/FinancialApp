"""Normal-load shape: a modest, steady plateau representing expected traffic.

Ramps to a steady user count, holds it, then stops. This is the baseline against
which the stress run is compared. Scale with LOADTEST_NORMAL_USERS /
LOADTEST_NORMAL_HOLD_SECONDS without editing code.
"""

import os

from locust import LoadTestShape


def _int_env(name, default):
	try:
		return int(os.getenv(name, str(default)))
	except ValueError:
		return default


class NormalLoad(LoadTestShape):

	USERS = _int_env("LOADTEST_NORMAL_USERS", 20)
	SPAWN_RATE = _int_env("LOADTEST_NORMAL_SPAWN_RATE", 2)
	RAMP_SECONDS = _int_env("LOADTEST_NORMAL_RAMP_SECONDS", 30)
	HOLD_SECONDS = _int_env("LOADTEST_NORMAL_HOLD_SECONDS", 300)

	def tick(self):
		run_time = self.get_run_time()
		if run_time > self.RAMP_SECONDS + self.HOLD_SECONDS:
			return None
		return (self.USERS, self.SPAWN_RATE)

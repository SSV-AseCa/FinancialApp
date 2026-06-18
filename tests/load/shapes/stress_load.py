"""Stress/peak-load shape: stepwise ramp to find the saturation knee.

Holds each successively higher user count for a fixed window so you can read, per
step, where p95 latency degrades sharply or errors appear. Scale the steps with
LOADTEST_STRESS_STEPS (comma-separated user counts) and LOADTEST_STRESS_STEP_SECONDS.
"""

import os

from locust import LoadTestShape


def _int_env(name, default):
	try:
		return int(os.getenv(name, str(default)))
	except ValueError:
		return default


def _steps_env(default):
	raw = os.getenv("LOADTEST_STRESS_STEPS")
	if not raw:
		return default
	try:
		return [int(part) for part in raw.split(",") if part.strip()]
	except ValueError:
		return default


class StressLoad(LoadTestShape):

	STEPS = _steps_env([25, 50, 100, 200, 400])
	STEP_SECONDS = _int_env("LOADTEST_STRESS_STEP_SECONDS", 60)
	SPAWN_RATE = _int_env("LOADTEST_STRESS_SPAWN_RATE", 20)

	def tick(self):
		run_time = self.get_run_time()
		step_index = int(run_time // self.STEP_SECONDS)
		if step_index >= len(self.STEPS):
			return None
		return (self.STEPS[step_index], self.SPAWN_RATE)

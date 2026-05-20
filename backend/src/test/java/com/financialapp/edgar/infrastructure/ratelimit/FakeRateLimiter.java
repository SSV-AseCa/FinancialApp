package com.financialapp.edgar.infrastructure.ratelimit;

public class FakeRateLimiter implements RateLimiter {

	private int acquireCalls;

	@Override
	public void acquire() {
		acquireCalls++;
	}

	public int acquireCalls() {
		return acquireCalls;
	}
}

package com.ssv.edgar.infrastructure.ratelimit;

import com.ssv.edgar.infrastructure.ratelimit.RateLimiter;

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

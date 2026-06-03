package com.ssv.edgar.infrastructure.ratelimit;

public interface RateLimiter {
	void acquire();
}

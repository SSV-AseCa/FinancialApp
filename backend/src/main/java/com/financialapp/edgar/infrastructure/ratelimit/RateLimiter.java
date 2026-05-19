package com.financialapp.edgar.infrastructure.ratelimit;

public interface RateLimiter {
	void acquire();
}

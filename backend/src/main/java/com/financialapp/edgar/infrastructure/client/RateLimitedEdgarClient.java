package com.financialapp.edgar.infrastructure.client;

import com.financialapp.edgar.application.EdgarClient;
import com.financialapp.edgar.infrastructure.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RateLimitedEdgarClient implements EdgarClient {

	private final EdgarClient delegate;
	private final RateLimiter rateLimiter;

	@Override
	public String get(String path) {
		rateLimiter.acquire();
		return delegate.get(path);
	}
}

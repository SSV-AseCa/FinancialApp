package com.ssv.edgar.infrastructure.client;

import java.time.Duration;

import com.ssv.cache.application.ResponseCache;
import com.ssv.edgar.application.EdgarClient;

/**
 * Read-through cache decorator for {@link EdgarClient}. A fresh cached payload
 * is returned without touching the rate limiter or the network; only on a miss
 * (or expiry) is the delegate called and its response cached. Transparent — the
 * {@code get(path)} contract is unchanged.
 */
public class CachingEdgarClient implements EdgarClient {

	private final EdgarClient delegate;
	private final ResponseCache cache;
	private final String provider;
	private final Duration ttl;

	public CachingEdgarClient(EdgarClient delegate, ResponseCache cache, String namespace, Duration ttl) {
		this.delegate = delegate;
		this.cache = cache;
		this.provider = "EDGAR:" + namespace;
		this.ttl = ttl;
	}

	@Override
	public String get(String path) {
		return cache.getOrLoad(provider, path, ttl, () -> delegate.get(path));
	}
}

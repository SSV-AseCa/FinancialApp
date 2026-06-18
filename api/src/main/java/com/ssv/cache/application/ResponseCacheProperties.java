package com.ssv.cache.application;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Time-to-live for each external provider's cached responses. Configured via
 * {@code app.cache.edgar-ttl} and {@code app.cache.market-ttl} (e.g.
 * {@code 1h}, {@code 5m}); both default to one hour.
 */
@ConfigurationProperties(prefix = "app.cache")
public record ResponseCacheProperties(Duration edgarTtl, Duration marketTtl) {

	public ResponseCacheProperties {
		edgarTtl = edgarTtl != null ? edgarTtl : Duration.ofHours(1);
		marketTtl = marketTtl != null ? marketTtl : Duration.ofHours(1);
	}
}

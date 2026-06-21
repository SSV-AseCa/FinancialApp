package com.ssv.cache.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ssv.cache.domain.CachedResponse;
import com.ssv.cache.infrastructure.persistence.CachedResponseRepository;

import lombok.RequiredArgsConstructor;

/**
 * Durable, time-limited read-through cache. {@link #getOrLoad} returns the
 * stored payload while it is fresh and otherwise calls {@code loader}, persists
 * the result with a new expiry, and returns it. Caching is best-effort: a
 * failure to read or write the cache never prevents the upstream value from
 * being returned.
 */
@Service
@RequiredArgsConstructor
public class ResponseCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCache.class);

	private final CachedResponseRepository repository;
	private final Clock clock;

	public String getOrLoad(String provider, String key, Duration ttl, Supplier<String> loader) {
		Optional<CachedResponse> hit = findQuietly(provider, key);
		if (hit.isPresent() && hit.get().isFresh(Instant.now(clock))) {
			return hit.get().getPayload();
		}
		String value = loader.get();
		upsert(provider, key, value, Instant.now(clock).plus(ttl));
		return value;
	}

	/**
	 * Like {@link #getOrLoad} but serves the last cached value when the loader
	 * fails — only an empty cache surfaces the failure (used for market prices).
	 */
	public String getOrServeStale(String provider, String key, Duration ttl, Supplier<String> loader) {
		Optional<CachedResponse> hit = findQuietly(provider, key);
		if (hit.isPresent() && hit.get().isFresh(Instant.now(clock))) {
			return hit.get().getPayload();
		}
		return refreshOrServeStale(provider, key, ttl, loader);
	}

	private String refreshOrServeStale(String provider, String key, Duration ttl, Supplier<String> loader) {
		try {
			String value = loader.get();
			upsert(provider, key, value, Instant.now(clock).plus(ttl));
			return value;
		} catch (RuntimeException exception) {
			return serveStale(provider, key, exception);
		}
	}

	private String serveStale(String provider, String key, RuntimeException cause) {
		Optional<CachedResponse> stale = findQuietly(provider, key);
		if (stale.isEmpty()) {
			throw cause;
		}
		LOGGER.warn("Refresh failed for {}:{} — serving stale cached value", provider, key, cause);
		return stale.get().getPayload();
	}

	private Optional<CachedResponse> findQuietly(String provider, String key) {
		try {
			return repository.findByProviderAndCacheKey(provider, key);
		} catch (RuntimeException exception) {
			LOGGER.warn("Cache lookup failed for {}:{} — treating as a miss", provider, key, exception);
			return Optional.empty();
		}
	}

	private void upsert(String provider, String key, String value, Instant expiresAt) {
		try {
			repository.save(entry(provider, key, value, expiresAt));
		} catch (RuntimeException exception) {
			LOGGER.warn("Cache store failed for {}:{} — skipping cache write", provider, key, exception);
		}
	}

	private CachedResponse entry(String provider, String key, String value, Instant expiresAt) {
		CachedResponse existing = repository.findByProviderAndCacheKey(provider, key).orElse(null);
		if (existing == null) {
			return new CachedResponse(provider, key, value, expiresAt);
		}
		existing.refresh(value, expiresAt);
		return existing;
	}
}

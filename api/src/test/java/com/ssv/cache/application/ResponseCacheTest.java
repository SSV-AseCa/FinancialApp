package com.ssv.cache.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.ssv.cache.domain.CachedResponse;
import com.ssv.cache.infrastructure.persistence.CachedResponseRepository;

class ResponseCacheTest {

	private static final String PROVIDER = "EDGAR:data";
	private static final String KEY = "/submissions/CIK0000320193.json";
	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
	private static final Duration TTL = Duration.ofHours(1);

	private final CachedResponseRepository repository = mock(CachedResponseRepository.class);
	private final ResponseCache cache = new ResponseCache(repository, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void loadsFromUpstreamAndStoresOnMiss() {
		when(repository.findByProviderAndCacheKey(PROVIDER, KEY)).thenReturn(Optional.empty());
		CountingSupplier loader = new CountingSupplier("fresh-body");

		String result = cache.getOrLoad(PROVIDER, KEY, TTL, loader);

		assertEquals("fresh-body", result);
		assertEquals(1, loader.calls());
		verify(repository).save(any(CachedResponse.class));
	}

	@Test
	void returnsCachedPayloadWithoutCallingUpstreamWhenFresh() {
		CachedResponse fresh = new CachedResponse(PROVIDER, KEY, "cached-body", NOW.plusSeconds(60));
		when(repository.findByProviderAndCacheKey(PROVIDER, KEY)).thenReturn(Optional.of(fresh));
		CountingSupplier loader = new CountingSupplier("should-not-run");

		String result = cache.getOrLoad(PROVIDER, KEY, TTL, loader);

		assertEquals("cached-body", result);
		assertEquals(0, loader.calls());
		verify(repository, never()).save(any(CachedResponse.class));
	}

	@Test
	void reloadsFromUpstreamWhenExpired() {
		CachedResponse stale = new CachedResponse(PROVIDER, KEY, "stale-body", NOW.minusSeconds(1));
		when(repository.findByProviderAndCacheKey(PROVIDER, KEY)).thenReturn(Optional.of(stale));
		CountingSupplier loader = new CountingSupplier("fresh-body");

		String result = cache.getOrLoad(PROVIDER, KEY, TTL, loader);

		assertEquals("fresh-body", result);
		assertEquals(1, loader.calls());
		verify(repository).save(stale);
	}

	@Test
	void returnsUpstreamValueEvenWhenCacheWriteFails() {
		when(repository.findByProviderAndCacheKey(eq(PROVIDER), eq(KEY))).thenReturn(Optional.empty());
		when(repository.save(any(CachedResponse.class))).thenThrow(new RuntimeException("db down"));
		CountingSupplier loader = new CountingSupplier("fresh-body");

		String result = cache.getOrLoad(PROVIDER, KEY, TTL, loader);

		assertEquals("fresh-body", result);
	}

	private static final class CountingSupplier implements Supplier<String> {

		private final String value;
		private final AtomicInteger calls = new AtomicInteger();

		private CountingSupplier(String value) {
			this.value = value;
		}

		@Override
		public String get() {
			calls.incrementAndGet();
			return value;
		}

		private int calls() {
			return calls.get();
		}
	}
}

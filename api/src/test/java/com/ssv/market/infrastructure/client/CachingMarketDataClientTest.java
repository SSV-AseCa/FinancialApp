package com.ssv.market.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ssv.cache.application.ResponseCache;
import com.ssv.cache.domain.CachedResponse;
import com.ssv.cache.infrastructure.persistence.CachedResponseRepository;
import com.ssv.market.application.MarketDataClient;
import com.ssv.market.application.dto.MarketPriceQuote;

class CachingMarketDataClientTest {

	private static final String PROVIDER = "YAHOO";
	private static final String SYMBOL = "AAPL";
	private static final LocalDate DATE = LocalDate.of(2024, 6, 3);
	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

	private final CachedResponseRepository repository = mock(CachedResponseRepository.class);
	private final ResponseCache cache = new ResponseCache(repository, Clock.fixed(NOW, ZoneOffset.UTC));
	private final MarketDataClient delegate = mock(MarketDataClient.class);
	private final CachingMarketDataClient client = new CachingMarketDataClient(delegate, cache, Duration.ofHours(1));

	@Test
	void fetchPriceAtLoadsFromDelegateAndCachesUnderASymbolDateKey() {
		when(repository.findByProviderAndCacheKey(PROVIDER, "AAPL@2024-06-03")).thenReturn(Optional.empty());
		when(delegate.fetchPriceAt(SYMBOL, DATE))
				.thenReturn(new MarketPriceQuote(SYMBOL, new BigDecimal("110"), "USD"));

		MarketPriceQuote quote = client.fetchPriceAt(SYMBOL, DATE);

		assertEquals(new BigDecimal("110"), quote.price());
		verify(delegate).fetchPriceAt(SYMBOL, DATE);
		verify(repository).save(any(CachedResponse.class));
	}

	@Test
	void fetchPriceAtServesTheCachedQuoteWithoutCallingTheDelegateWhenFresh() {
		String payload = "{\"symbol\":\"AAPL\",\"price\":110,\"currency\":\"USD\"}";
		CachedResponse fresh = new CachedResponse(PROVIDER, "AAPL@2024-06-03", payload, NOW.plusSeconds(60));
		when(repository.findByProviderAndCacheKey(PROVIDER, "AAPL@2024-06-03")).thenReturn(Optional.of(fresh));

		MarketPriceQuote quote = client.fetchPriceAt(SYMBOL, DATE);

		assertEquals(new BigDecimal("110"), quote.price());
		verify(delegate, org.mockito.Mockito.never()).fetchPriceAt(SYMBOL, DATE);
	}
}

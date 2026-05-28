package com.ssv.market.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ssv.market.domain.MarketPrice;
import com.ssv.market.infrastructure.config.MarketPriceProperties;
import com.ssv.market.infrastructure.persistence.MarketPriceRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketPriceServiceTest {

	private static final String SYMBOL = "AAPL";
	private static final String SOURCE = "yahoo-finance";

	@Test
	void shouldFetchAndStorePrice() {
		MarketPriceRepository repository = mock(MarketPriceRepository.class);
		FakeMarketDataClient client = new FakeMarketDataClient();
		MarketPriceService service = service(client, repository);
		when(repository.save(any(MarketPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MarketPrice saved = service.fetchAndStore(SYMBOL);

		assertEquals(SYMBOL, client.fetchedSymbol());
		assertEquals(SOURCE, saved.getSource());
		verify(repository).save(any(MarketPrice.class));
	}

	@Test
	void shouldReturnLatestStoredPrice() {
		MarketPriceRepository repository = mock(MarketPriceRepository.class);
		MarketPriceService service = service(new FakeMarketDataClient(), repository);
		when(repository.findTopBySymbolOrderByFetchedAtDesc(SYMBOL)).thenReturn(Optional.empty());

		Optional<MarketPrice> price = service.getLatestPrice(SYMBOL);

		assertEquals(Optional.empty(), price);
	}

	private MarketPriceService service(MarketDataClient client, MarketPriceRepository repository) {
		return new MarketPriceService(client, repository, properties(), clock());
	}

	private MarketPriceProperties properties() {
		return new MarketPriceProperties(1000L, java.util.List.of(SYMBOL), SOURCE, "http://localhost", "/%s");
	}

	private Clock clock() {
		return Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
	}
}

package com.ssv.market.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.service.MarketPriceService;
import com.ssv.market.fake.FakeMarketPriceRepository;
import org.junit.jupiter.api.Test;

import com.ssv.market.domain.MarketPrice;

class MarketPriceServiceTest {

	private static final String SYMBOL = "AAPL";
	private static final String SOURCE = "yahoo-finance";

	@Test
	void shouldFetchAndStorePrice() {
		FakeMarketPriceRepository fakeRepo = new FakeMarketPriceRepository();
		FakeMarketDataClient client = new FakeMarketDataClient();
		MarketPriceService service = service(client, fakeRepo);

		MarketPrice saved = service.fetchAndStore(SYMBOL);

		assertEquals(SYMBOL, client.fetchedSymbol());
		assertEquals(SOURCE, saved.getSource());
		assertEquals(saved, fakeRepo.lastSaved());
	}

	@Test
	void shouldReturnLatestStoredPrice() {
		FakeMarketPriceRepository fakeRepo = new FakeMarketPriceRepository();
		MarketPriceService service = service(new FakeMarketDataClient(), fakeRepo);

		Optional<MarketPrice> price = service.getLatestPrice(SYMBOL);

		assertEquals(Optional.empty(), price);
	}

	private MarketPriceService service(MarketDataClient client, FakeMarketPriceRepository repository) {
		return new MarketPriceService(client, repository, properties(), clock());
	}

	private Clock clock() {
		return Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
	}

	private MarketPriceProperties properties() {
		return new MarketPriceProperties(1000L, SOURCE, "http://localhost", "/%s", "test-api-key");
	}
}

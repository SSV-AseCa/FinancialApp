package com.ssv.market.infrastructure.scheduler;

import java.util.List;

import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.service.MarketPriceService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MarketPriceSchedulerTest {

	@Test
	void shouldFetchAllConfiguredSymbols() {
		MarketPriceService service = mock(MarketPriceService.class);
		MarketPriceScheduler scheduler = new MarketPriceScheduler(service, properties());

		scheduler.fetchConfiguredSymbols();

		verify(service).fetchAndStore("AAPL");
		verify(service).fetchAndStore("MSFT");
	}

	@Test
	void shouldContinueWhenOneSymbolFails() {
		MarketPriceService service = mock(MarketPriceService.class);
		MarketPriceScheduler scheduler = new MarketPriceScheduler(service, properties());
		doThrow(new IllegalStateException("error")).when(service).fetchAndStore("AAPL");

		scheduler.fetchConfiguredSymbols();

		verify(service).fetchAndStore("MSFT");
	}

	private MarketPriceProperties properties() {
		return new MarketPriceProperties(1000L, List.of("AAPL", "MSFT"), "yahoo-finance", "base", "path");
	}
}

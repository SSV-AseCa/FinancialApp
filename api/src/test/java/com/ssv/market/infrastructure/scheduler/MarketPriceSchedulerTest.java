package com.ssv.market.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ssv.market.fake.FakeMarketPriceService;
import com.ssv.portfolio.fake.FakePortfolioPositionQueryService;

class MarketPriceSchedulerTest {

	@Test
	void shouldFetchOnlyPortfolioSymbols() {
		FakeMarketPriceService fakeService = new FakeMarketPriceService();
		FakePortfolioPositionQueryService fakeQueryService = new FakePortfolioPositionQueryService();
		fakeQueryService.respondWith(List.of("AAPL", "MSFT"));

		MarketPriceScheduler scheduler = new MarketPriceScheduler(fakeService, fakeQueryService);
		scheduler.fetchPortfolioSymbols();

		assertTrue(fakeService.fetchedSymbols().contains("AAPL"));
		assertTrue(fakeService.fetchedSymbols().contains("MSFT"));
	}

	@Test
	void shouldContinueWhenOneSymbolFails() {
		FakeMarketPriceService fakeService = new FakeMarketPriceService();
		FakePortfolioPositionQueryService fakeQueryService = new FakePortfolioPositionQueryService();
		fakeQueryService.respondWith(List.of("AAPL", "MSFT"));
		fakeService.throwOnFetch("AAPL", new IllegalStateException("error"));

		MarketPriceScheduler scheduler = new MarketPriceScheduler(fakeService, fakeQueryService);
		scheduler.fetchPortfolioSymbols();

		assertTrue(fakeService.fetchedSymbols().contains("MSFT"));
		assertFalse(fakeService.fetchedSymbols().contains("AAPL"));
	}
}

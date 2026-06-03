package com.ssv.market.infrastructure.scheduler;

import java.util.List;

import com.ssv.market.application.service.MarketPriceService;
import com.ssv.portfolio.application.PortfolioPositionQueryService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketPriceSchedulerTest {

	@Test
	void shouldFetchOnlyPortfolioSymbols() {
		MarketPriceService service = mock(MarketPriceService.class);
		PortfolioPositionQueryService serviceQuery = mock(PortfolioPositionQueryService.class);

		when(serviceQuery.findDistinctSymbols()).thenReturn(List.of("AAPL", "MSFT"));

		MarketPriceScheduler scheduler = new MarketPriceScheduler(service, serviceQuery);

		scheduler.fetchPortfolioSymbols();

		verify(service).fetchAndStore("AAPL");
		verify(service).fetchAndStore("MSFT");
	}

	@Test
	void shouldContinueWhenOneSymbolFails() {
		MarketPriceService service = mock(MarketPriceService.class);
		PortfolioPositionQueryService serviceQuery = mock(PortfolioPositionQueryService.class);

		when(serviceQuery.findDistinctSymbols()).thenReturn(List.of("AAPL", "MSFT"));
		doThrow(new IllegalStateException("error")).when(service).fetchAndStore("AAPL");

		MarketPriceScheduler scheduler = new MarketPriceScheduler(service, serviceQuery);

		scheduler.fetchPortfolioSymbols();

		verify(service).fetchAndStore("MSFT");
	}
}

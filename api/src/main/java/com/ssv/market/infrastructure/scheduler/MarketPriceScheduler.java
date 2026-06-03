package com.ssv.market.infrastructure.scheduler;

import com.ssv.market.application.service.MarketPriceService;
import com.ssv.portfolio.application.PortfolioPositionQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MarketPriceScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MarketPriceScheduler.class);

	private final MarketPriceService service;
	private final PortfolioPositionQueryService positionQueryService;

	@Scheduled(fixedDelayString = "${market.prices.fetch-frequency-ms}")
	public void fetchPortfolioSymbols() {
		for (String symbol : positionQueryService.findDistinctSymbols()) {
			fetchSymbol(symbol);
		}
	}

	private void fetchSymbol(String symbol) {
		try {
			service.fetchAndStore(symbol);
			LOGGER.info("Fetched market price for {}", symbol);
		} catch (RuntimeException exception) {
			LOGGER.warn("Could not fetch market price for {}", symbol, exception);
		}
	}
}

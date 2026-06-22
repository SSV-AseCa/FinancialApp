package com.ssv.market.application;

import com.ssv.market.application.dto.MarketPriceQuote;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FakeMarketDataClient implements MarketDataClient {

	private String fetchedSymbol;

	@Override
	public MarketPriceQuote fetchPrice(String symbol) {
		fetchedSymbol = symbol;
		return new MarketPriceQuote(symbol, BigDecimal.TEN, "USD");
	}

	@Override
	public MarketPriceQuote fetchPriceAt(String symbol, LocalDate date) {
		fetchedSymbol = symbol;
		return new MarketPriceQuote(symbol, BigDecimal.TEN, "USD");
	}

	String fetchedSymbol() {
		return fetchedSymbol;
	}
}

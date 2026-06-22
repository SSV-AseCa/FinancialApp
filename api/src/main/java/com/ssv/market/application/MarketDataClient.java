package com.ssv.market.application;

import java.time.LocalDate;

import com.ssv.market.application.dto.MarketPriceQuote;

public interface MarketDataClient {

	MarketPriceQuote fetchPrice(String symbol);

	MarketPriceQuote fetchPriceAt(String symbol, LocalDate date);
}

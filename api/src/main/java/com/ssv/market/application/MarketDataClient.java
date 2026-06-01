package com.ssv.market.application;

import com.ssv.market.application.dto.MarketPriceQuote;

public interface MarketDataClient {

	MarketPriceQuote fetchPrice(String symbol);
}

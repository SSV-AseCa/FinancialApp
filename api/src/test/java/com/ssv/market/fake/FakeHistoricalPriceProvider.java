package com.ssv.market.fake;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ssv.market.application.HistoricalPriceProvider;

/**
 * In-memory {@link HistoricalPriceProvider} for tests. A symbol/date pair
 * without a stubbed price returns empty, mirroring an unavailable historical
 * quote.
 */
public class FakeHistoricalPriceProvider implements HistoricalPriceProvider {

	private final Map<String, BigDecimal> prices = new HashMap<>();

	@Override
	public Optional<BigDecimal> priceAt(String symbol, LocalDate date) {
		return Optional.ofNullable(prices.get(key(symbol, date)));
	}

	public FakeHistoricalPriceProvider stub(String symbol, LocalDate date, BigDecimal price) {
		prices.put(key(symbol, date), price);
		return this;
	}

	private static String key(String symbol, LocalDate date) {
		return symbol + "@" + date;
	}
}

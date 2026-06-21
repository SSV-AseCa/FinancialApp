package com.ssv.market.fake;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ssv.market.application.CurrentPriceProvider;

/**
 * In-memory {@link CurrentPriceProvider} for tests. Symbols without a stubbed
 * price return empty, mirroring an unavailable upstream quote.
 */
public class FakeCurrentPriceProvider implements CurrentPriceProvider {

	private final Map<String, BigDecimal> prices = new HashMap<>();
	private BigDecimal defaultPrice;

	@Override
	public Optional<BigDecimal> currentPrice(String symbol) {
		return Optional.ofNullable(prices.getOrDefault(symbol, defaultPrice));
	}

	public FakeCurrentPriceProvider stub(String symbol, BigDecimal price) {
		prices.put(symbol, price);
		return this;
	}

	/** Price returned for any symbol without an explicit stub. */
	public FakeCurrentPriceProvider withDefault(BigDecimal price) {
		this.defaultPrice = price;
		return this;
	}

	/** Clears all stubs and any default, isolating a shared bean between tests. */
	public void reset() {
		prices.clear();
		defaultPrice = null;
	}
}

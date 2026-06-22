package com.ssv.portfolio.application;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssv.market.application.CurrentPriceProvider;
import com.ssv.market.application.HistoricalPriceProvider;
import com.ssv.portfolio.domain.Position;
import com.ssv.shared.exceptions.MarketPriceFetchException;

/**
 * Values a {@link Position}. Current value is the latest market price times the
 * quantity held; cost basis is the price on the acquisition date times the
 * quantity. A price that cannot be fetched is never treated as zero: valuation
 * fails closed so a stale or unavailable price cannot masquerade as a real
 * number.
 */
public final class PortfolioValuationCalculator {

	private PortfolioValuationCalculator() {
	}

	public static BigDecimal currentValue(Position position, CurrentPriceProvider priceProvider) {
		BigDecimal price = priceProvider.currentPrice(position.getTicker()).orElseThrow(
				() -> new MarketPriceFetchException("No market price available for " + position.getTicker()));
		return price.multiply(BigDecimal.valueOf(position.getQuantity()));
	}

	public static BigDecimal costBasis(Position position) {
		return position.getCostBasis() == null ? BigDecimal.ZERO : position.getCostBasis();
	}

	/**
	 * Cost basis as of the acquisition date: the historical price on that date
	 * times the quantity, so P&amp;L reflects the gain since acquisition. Fails
	 * closed when no historical price is available for the date.
	 */
	public static BigDecimal costBasisAt(HistoricalPriceProvider priceProvider, String symbol, int quantity,
			LocalDate date) {
		BigDecimal price = priceProvider.priceAt(symbol, date).orElseThrow(
				() -> new MarketPriceFetchException("No market price available for " + symbol + " on " + date));
		return price.multiply(BigDecimal.valueOf(quantity));
	}
}

package com.ssv.portfolio.application;

import java.math.BigDecimal;

import com.ssv.market.application.CurrentPriceProvider;
import com.ssv.portfolio.domain.Position;

/**
 * Values a {@link Position}. Current value is the latest market price times the
 * quantity held; cost basis is the amount recorded when the shares were bought
 * (no reconstruction from price history). A position with no available price
 * contributes zero current value, and one with no recorded cost basis a zero
 * basis.
 */
public final class PortfolioValuationCalculator {

	private PortfolioValuationCalculator() {
	}

	public static BigDecimal currentValue(Position position, CurrentPriceProvider priceProvider) {
		return priceProvider.currentPrice(position.getTicker())
				.map(price -> price.multiply(BigDecimal.valueOf(position.getQuantity()))).orElse(BigDecimal.ZERO);
	}

	public static BigDecimal costBasis(Position position) {
		return position.getCostBasis() == null ? BigDecimal.ZERO : position.getCostBasis();
	}
}

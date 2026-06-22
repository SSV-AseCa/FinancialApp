package com.ssv.market.application;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ssv.shared.exceptions.MarketPriceFetchException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

/**
 * Serves the latest price through {@link MarketDataClient}, which is itself a
 * read-through cache over the upstream provider. A failure to reach the
 * provider surfaces as an empty result rather than an exception, leaving the
 * decision of how to treat an unavailable price to the caller.
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class MarketDataCurrentPriceProvider implements CurrentPriceProvider {

	private final MarketDataClient marketDataClient;

	@Override
	public Optional<BigDecimal> currentPrice(String symbol) {
		try {
			return Optional.of(marketDataClient.fetchPrice(symbol).price());
		} catch (MarketPriceFetchException exception) {
			return Optional.empty();
		}
	}
}

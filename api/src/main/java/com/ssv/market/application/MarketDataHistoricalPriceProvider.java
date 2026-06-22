package com.ssv.market.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ssv.shared.exceptions.MarketPriceFetchException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

/**
 * Serves a symbol's price as of a past date through {@link MarketDataClient},
 * which is itself a read-through cache over the upstream provider. A failure to
 * reach the provider surfaces as an empty result rather than an exception, so a
 * single unpriced symbol cannot break a position write.
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class MarketDataHistoricalPriceProvider implements HistoricalPriceProvider {

	private final MarketDataClient marketDataClient;

	@Override
	public Optional<BigDecimal> priceAt(String symbol, LocalDate date) {
		try {
			return Optional.of(marketDataClient.fetchPriceAt(symbol, date).price());
		} catch (MarketPriceFetchException exception) {
			return Optional.empty();
		}
	}
}

package com.ssv.market.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Application-layer port for the market price of a symbol as of a past date.
 * Used to anchor a manually recorded position's cost basis to its acquisition
 * date rather than to the moment of data entry. An empty result means no
 * historical price is available (e.g. the upstream provider is unreachable or
 * has no bar on or before the requested date).
 */
public interface HistoricalPriceProvider {

	Optional<BigDecimal> priceAt(String symbol, LocalDate date);
}

package com.ssv.market.application;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Application-layer port for the latest market price of a symbol. Returns the
 * price on demand so portfolio valuation reads do not depend on a background
 * price-sampling job. An empty result means no price is currently available
 * (e.g. the upstream provider is unreachable), letting callers decide whether
 * to degrade gracefully or fail.
 */
public interface CurrentPriceProvider {

	Optional<BigDecimal> currentPrice(String symbol);
}

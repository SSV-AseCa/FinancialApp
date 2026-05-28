package com.ssv.market.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPriceCreateRequest(String symbol, BigDecimal price, String currency, Instant fetchedAt,
		String source) {
}

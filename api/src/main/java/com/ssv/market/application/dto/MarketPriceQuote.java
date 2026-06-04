package com.ssv.market.application.dto;

import java.math.BigDecimal;

public record MarketPriceQuote(String symbol, BigDecimal price, String currency) {
}

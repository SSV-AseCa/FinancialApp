package com.ssv.portfolio.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PositionPnlResponse(UUID id, String ticker, int quantity, BigDecimal costBasis, BigDecimal currentPrice,
		BigDecimal currentValue, BigDecimal pnl) {
}

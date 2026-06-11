package com.ssv.portfolio.dto;

import java.math.BigDecimal;

public record PortfolioPerformanceResponse(BigDecimal totalValue, BigDecimal totalPnL) {
}

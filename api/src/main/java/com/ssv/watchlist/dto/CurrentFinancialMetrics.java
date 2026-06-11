package com.ssv.watchlist.dto;

import java.math.BigDecimal;

public record CurrentFinancialMetrics(BigDecimal revenue, BigDecimal netIncome, BigDecimal assets, BigDecimal equity) {
}

package com.ssv.company.dto;

import java.math.BigDecimal;

public record CurrentCompanyMetrics(BigDecimal revenue, BigDecimal netIncome, BigDecimal assets, BigDecimal equity) {
}

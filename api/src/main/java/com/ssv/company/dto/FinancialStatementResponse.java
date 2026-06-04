package com.ssv.company.dto;

import java.math.BigDecimal;

public record FinancialStatementResponse(String metric, BigDecimal value, String unit, String periodEnd) {
}

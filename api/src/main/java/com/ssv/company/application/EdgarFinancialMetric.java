package com.ssv.company.application;

import java.math.BigDecimal;

public record EdgarFinancialMetric(String metric, BigDecimal value, String unit, String periodEnd) {
}

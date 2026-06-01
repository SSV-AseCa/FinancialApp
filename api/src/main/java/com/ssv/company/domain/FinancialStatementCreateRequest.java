package com.ssv.company.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record FinancialStatementCreateRequest(Company company, String metric, BigDecimal value, String unit,
		String periodEnd, Instant fetchedAt) {
}

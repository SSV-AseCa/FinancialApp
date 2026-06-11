package com.ssv.company.dto;

import java.math.BigDecimal;

public record CompanyHistoryPoint(String period, BigDecimal revenue, BigDecimal netIncome, BigDecimal assets,
		BigDecimal equity) {
}

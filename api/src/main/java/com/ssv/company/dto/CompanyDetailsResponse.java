package com.ssv.company.dto;

import java.util.List;

public record CompanyDetailsResponse(
		String cik,
		String symbol,
		String name,
		List<FinancialStatementResponse> financialMetrics,
		List<SecFilingResponse> filings) {
}

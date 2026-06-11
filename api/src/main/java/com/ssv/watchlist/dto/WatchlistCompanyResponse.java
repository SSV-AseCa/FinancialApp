package com.ssv.watchlist.dto;

import java.util.UUID;

public record WatchlistCompanyResponse(UUID companyId, String cik, String symbol, String name,
		CurrentFinancialMetrics metrics) {
}

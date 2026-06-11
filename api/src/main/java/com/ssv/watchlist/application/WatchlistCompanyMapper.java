package com.ssv.watchlist.application;

import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.domain.Company;
import com.ssv.watchlist.dto.CurrentFinancialMetrics;
import com.ssv.watchlist.dto.WatchlistCompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WatchlistCompanyMapper {

	private final CompanyMetricsService companyMetricsService;

	public WatchlistCompanyResponse toResponse(Company company) {
		return new WatchlistCompanyResponse(company.getId(), company.getCik(), company.getSymbol(), company.getName(),
				fetchMetrics(company));
	}

	private CurrentFinancialMetrics fetchMetrics(Company company) {
		try {
			var metrics = companyMetricsService.currentMetrics(company.getCik());
			return toMetrics(metrics);
		} catch (RuntimeException exception) {
			return null;
		}
	}

	private CurrentFinancialMetrics toMetrics(com.ssv.company.dto.CurrentCompanyMetrics metrics) {
		return new CurrentFinancialMetrics(metrics.revenue(), metrics.netIncome(), metrics.assets(), metrics.equity());
	}
}

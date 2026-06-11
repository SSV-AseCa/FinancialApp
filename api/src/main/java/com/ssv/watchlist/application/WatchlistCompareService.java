package com.ssv.watchlist.application;

import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.CikUtils;
import com.ssv.company.domain.Company;
import com.ssv.watchlist.dto.CurrentFinancialMetrics;
import com.ssv.watchlist.dto.WatchlistCompareCompanyResponse;
import com.ssv.watchlist.dto.WatchlistCompareResponse;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchlistCompareService {

	private static final int MIN_COMPARE_SIZE = 2;

	private final WatchlistRepository watchlistRepository;
	private final CompanyStore companyStore;
	private final CompanyMetricsService companyMetricsService;

	@Transactional(readOnly = true)
	public WatchlistCompareResponse compare(UUID investorId, String ciksParam) {
		Set<String> ciks = parseCiks(ciksParam);
		validateCompareSize(ciks);
		return new WatchlistCompareResponse(ciks.stream().map(cik -> compareCompany(investorId, cik)).toList());
	}

	private Set<String> parseCiks(String ciksParam) {
		validateCiksParam(ciksParam);
		Set<String> ciks = new LinkedHashSet<>();
		Arrays.stream(ciksParam.split(",")).map(String::strip).filter(raw -> !raw.isEmpty()).map(CikUtils::normalize)
				.forEach(ciks::add);
		return ciks;
	}

	private void validateCiksParam(String ciksParam) {
		if (ciksParam == null || ciksParam.isBlank()) {
			throw new IllegalArgumentException("ciks parameter is required");
		}
	}

	private void validateCompareSize(Set<String> ciks) {
		if (ciks.size() < MIN_COMPARE_SIZE) {
			throw new IllegalArgumentException("At least two distinct CIKs must be provided");
		}
	}

	private WatchlistCompareCompanyResponse compareCompany(UUID investorId, String cik) {
		Company company = findCompany(cik);
		ensureWatched(investorId, company, cik);
		return toResponse(company, cik);
	}

	private Company findCompany(String cik) {
		return companyStore.findByCik(cik).orElseThrow(() -> new IllegalArgumentException("Unknown CIK: " + cik));
	}

	private void ensureWatched(UUID investorId, Company company, String cik) {
		if (!watchlistRepository.existsByInvestorIdAndCompanyId(investorId, company.getId())) {
			throw new IllegalArgumentException("CIK not on watchlist: " + cik);
		}
	}

	private WatchlistCompareCompanyResponse toResponse(Company company, String cik) {
		return new WatchlistCompareCompanyResponse(company.getId(), cik, company.getSymbol(), company.getName(),
				metricsFor(cik));
	}

	private CurrentFinancialMetrics metricsFor(String cik) {
		var metrics = companyMetricsService.currentMetrics(cik);
		return new CurrentFinancialMetrics(metrics.revenue(), metrics.netIncome(), metrics.assets(), metrics.equity());
	}
}

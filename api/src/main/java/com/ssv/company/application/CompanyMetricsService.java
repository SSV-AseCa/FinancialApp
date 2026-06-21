package com.ssv.company.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.dto.CurrentCompanyMetrics;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.infrastructure.persistence.FinancialStatementRepository;
import com.ssv.shared.dto.PageResponse;
import com.ssv.shared.exceptions.EdgarUnavailableException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyMetricsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyMetricsService.class);

	private final CompanyProvisioningService companyProvisioningService;
	private final CompanyFinancialDataRefresher refresher;
	private final FinancialStatementRepository financialStatementRepository;
	private final CompanyHistoryService companyHistoryService;

	public PageResponse<FinancialMetricResponse> getMetrics(String cik, String query, Pageable pageable) {
		var company = companyProvisioningService.ensureCompany(cik);
		refreshQuietly(company);
		var page = (query == null || query.isBlank())
				? financialStatementRepository.findByCompanyId(company.getId(), pageable)
				: financialStatementRepository.findByCompanyIdAndMetricContainingIgnoreCase(company.getId(),
						query.strip(), pageable);
		return PageResponse.of(page.map(this::toMetricResponse));
	}

	/**
	 * Refreshes from EDGAR but tolerates an unavailable provider: a known company
	 * keeps serving whatever financial data is already persisted rather than
	 * failing the request when EDGAR is down or rate-limiting.
	 */
	private void refreshQuietly(Company company) {
		try {
			refresher.refreshIfStale(company);
		} catch (EdgarUnavailableException exception) {
			LOGGER.warn("EDGAR refresh failed for CIK {} — serving cached data", company.getCik(), exception);
		}
	}

	private FinancialMetricResponse toMetricResponse(FinancialStatement fs) {
		return new FinancialMetricResponse(fs.getMetric(), fs.getValue(), fs.getUnit(), fs.getPeriodEnd());
	}

	public CurrentCompanyMetrics currentMetrics(String cik) {
		var history = companyHistoryService.historyByCik(cik);
		if (history.isEmpty()) {
			return new CurrentCompanyMetrics(null, null, null, null);
		}
		var latest = history.get(history.size() - 1);
		return new CurrentCompanyMetrics(latest.revenue(), latest.netIncome(), latest.assets(), latest.equity());
	}
}

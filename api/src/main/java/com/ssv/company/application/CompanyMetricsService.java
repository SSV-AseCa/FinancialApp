package com.ssv.company.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.dto.CompanyHistoryPoint;
import com.ssv.company.dto.CurrentCompanyMetrics;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.infrastructure.persistence.FinancialStatementRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyMetricsService {

	private final CompanyProvisioningService companyProvisioningService;
	private final CompanyFinancialDataRefresher refresher;
	private final FinancialStatementRepository financialStatementRepository;
	private final CompanyHistoryService companyHistoryService;

	public List<FinancialMetricResponse> getMetrics(String cik) {
		Company company = companyProvisioningService.ensureCompany(cik);
		refresher.refreshIfStale(company);
		return financialStatementRepository.findByCompanyId(company.getId()).stream().map(this::toResponse).toList();
	}

	public CurrentCompanyMetrics currentMetrics(String cik) {
		List<CompanyHistoryPoint> history = companyHistoryService.historyByCik(cik);
		if (history.isEmpty()) {
			return new CurrentCompanyMetrics(null, null, null, null);
		}
		CompanyHistoryPoint latest = history.get(history.size() - 1);
		return new CurrentCompanyMetrics(latest.revenue(), latest.netIncome(), latest.assets(), latest.equity());
	}

	private FinancialMetricResponse toResponse(FinancialStatement fs) {
		return new FinancialMetricResponse(fs.getMetric(), fs.getValue(), fs.getUnit(), fs.getPeriodEnd());
	}
}

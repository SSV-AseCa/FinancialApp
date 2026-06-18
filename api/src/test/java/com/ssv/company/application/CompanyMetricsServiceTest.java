package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.fake.FakeCompanyFinancialDataRefresher;
import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.application.fake.FakeFinancialDataProperties;
import com.ssv.company.application.fake.FakeFinancialStatementRepository;
import com.ssv.edgar.application.EdgarCompanyProfileParser;
import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.FinancialStatementCreateRequest;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.shared.exceptions.EdgarUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompanyMetricsServiceTest {

	private FakeCompanyStore companyStore;
	private FakeCompanyFinancialDataRefresher refresher;
	private FakeFinancialStatementRepository statementRepository;
	private CompanyMetricsService service;

	@BeforeEach
	void setUp() {
		companyStore = new FakeCompanyStore();
		refresher = new FakeCompanyFinancialDataRefresher();
		statementRepository = new FakeFinancialStatementRepository();
		CompanyProvisioningService provisioning = new CompanyProvisioningService(companyStore,
				new FakeEdgarClient(null), new EdgarCompanyProfileParser(new ObjectMapper()),
				new FakeFinancialDataProperties());
		service = new CompanyMetricsService(provisioning, refresher, statementRepository, null);
	}

	@Test
	void throwsCompanyNotFoundWhenCikUnknown() {
		assertThrows(CompanyNotFoundException.class, () -> service.getMetrics("9999999999"));
	}

	@Test
	void callsRefreshIfStaleBeforeReturningMetrics() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);

		service.getMetrics("0000320193");

		assertEquals(company, refresher.lastRefreshed());
	}

	@Test
	void returnsEmptyListWhenNoMetricsStored() {
		companyStore.seed(new Company("0000320193", "AAPL", "Apple Inc."));

		List<FinancialMetricResponse> result = service.getMetrics("0000320193");

		assertTrue(result.isEmpty());
	}

	@Test
	void returnsMappedMetricsForKnownCompany() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		FinancialStatement statement = new FinancialStatement(new FinancialStatementCreateRequest(company, "Revenue",
				new BigDecimal("394328000000"), "USD", "2023-09-30", Instant.now()));
		statementRepository.seed(company.getId(), List.of(statement));

		List<FinancialMetricResponse> result = service.getMetrics("0000320193");

		assertEquals(1, result.size());
		assertEquals("Revenue", result.get(0).metric());
		assertEquals(new BigDecimal("394328000000"), result.get(0).value());
		assertEquals("USD", result.get(0).unit());
		assertEquals("2023-09-30", result.get(0).periodEnd());
	}

	@Test
	void servesCachedMetricsWhenEdgarRefreshFails() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		FinancialStatement statement = new FinancialStatement(new FinancialStatementCreateRequest(company, "Revenue",
				new BigDecimal("394328000000"), "USD", "2023-09-30", Instant.now()));
		statementRepository.seed(company.getId(), List.of(statement));
		refresher.failWith(new EdgarUnavailableException("EDGAR down", new RuntimeException()));

		List<FinancialMetricResponse> result = service.getMetrics("0000320193");

		assertEquals(1, result.size());
		assertEquals("Revenue", result.get(0).metric());
	}
}

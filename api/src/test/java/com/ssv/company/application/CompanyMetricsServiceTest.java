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
import com.ssv.shared.dto.PageResponse;
import com.ssv.shared.exceptions.EdgarUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class CompanyMetricsServiceTest {

	private FakeCompanyStore companyStore;
	private FakeCompanyFinancialDataRefresher refresher;
	private FakeFinancialStatementRepository statementRepository;
	private CompanyMetricsService service;

	private static final Pageable FIRST_PAGE = PageRequest.of(0, 20);

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
		assertThrows(CompanyNotFoundException.class, () -> service.getMetrics("9999999999", null, FIRST_PAGE));
	}

	@Test
	void callsRefreshIfStaleBeforeReturningMetrics() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);

		service.getMetrics("0000320193", null, FIRST_PAGE);

		assertEquals(company, refresher.lastRefreshed());
	}

	@Test
	void returnsEmptyPageWhenNoMetricsStored() {
		companyStore.seed(new Company("0000320193", "AAPL", "Apple Inc."));

		PageResponse<FinancialMetricResponse> result = service.getMetrics("0000320193", null, FIRST_PAGE);

		assertTrue(result.content().isEmpty());
		assertEquals(0, result.totalElements());
	}

	@Test
	void returnsMappedMetricsForKnownCompany() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		FinancialStatement statement = new FinancialStatement(new FinancialStatementCreateRequest(company, "Revenue",
				new BigDecimal("394328000000"), "USD", "2023-09-30", Instant.now()));
		statementRepository.seed(company.getId(), List.of(statement));

		PageResponse<FinancialMetricResponse> result = service.getMetrics("0000320193", null, FIRST_PAGE);

		assertEquals(1, result.content().size());
		assertEquals("Revenue", result.content().get(0).metric());
		assertEquals(new BigDecimal("394328000000"), result.content().get(0).value());
		assertEquals("USD", result.content().get(0).unit());
		assertEquals("2023-09-30", result.content().get(0).periodEnd());
	}

	@Test
	void filtersMetricsByQueryOnMetricName() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		statementRepository.seed(company.getId(),
				List.of(statement(company, "Revenues"), statement(company, "Assets")));

		PageResponse<FinancialMetricResponse> result = service.getMetrics("0000320193", "rev", FIRST_PAGE);

		assertEquals(1, result.totalElements());
		assertEquals("Revenues", result.content().get(0).metric());
	}

	@Test
	void paginatesMetrics() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		statementRepository.seed(company.getId(),
				List.of(statement(company, "A"), statement(company, "B"), statement(company, "C")));

		PageResponse<FinancialMetricResponse> firstPage = service.getMetrics("0000320193", null, PageRequest.of(0, 2));
		PageResponse<FinancialMetricResponse> secondPage = service.getMetrics("0000320193", null, PageRequest.of(1, 2));

		assertEquals(3, firstPage.totalElements());
		assertEquals(2, firstPage.totalPages());
		assertEquals(2, firstPage.content().size());
		assertEquals(1, secondPage.content().size());
	}

	@Test
	void servesCachedMetricsWhenEdgarRefreshFails() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		statementRepository.seed(company.getId(), List.of(statement(company, "Revenue")));
		refresher.failWith(new EdgarUnavailableException("EDGAR down", new RuntimeException()));

		PageResponse<FinancialMetricResponse> result = service.getMetrics("0000320193", null, FIRST_PAGE);

		assertEquals(1, result.content().size());
		assertEquals("Revenue", result.content().get(0).metric());
	}

	private static FinancialStatement statement(Company company, String metric) {
		return new FinancialStatement(new FinancialStatementCreateRequest(company, metric,
				new BigDecimal("394328000000"), "USD", "2023-09-30", Instant.now()));
	}
}

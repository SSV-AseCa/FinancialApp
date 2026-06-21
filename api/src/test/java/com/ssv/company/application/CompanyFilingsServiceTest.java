package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.fake.FakeCompanyFinancialDataRefresher;
import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.application.fake.FakeFinancialDataProperties;
import com.ssv.company.domain.Company;
import com.ssv.company.domain.SecFiling;
import com.ssv.company.domain.SecFilingCreateRequest;
import com.ssv.company.dto.SecFilingResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.company.infrastructure.persistence.SecFilingRepository;
import com.ssv.edgar.application.EdgarCompanyProfileParser;
import com.ssv.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class CompanyFilingsServiceTest {

	private FakeCompanyStore companyStore;
	private FakeCompanyFinancialDataRefresher refresher;
	private SecFilingRepository secFilingRepository;
	private CompanyFilingsService service;

	private static final Pageable FIRST_PAGE = PageRequest.of(0, 20);

	@BeforeEach
	void setUp() {
		companyStore = new FakeCompanyStore();
		refresher = new FakeCompanyFinancialDataRefresher();
		secFilingRepository = mock(SecFilingRepository.class);
		CompanyProvisioningService provisioning = new CompanyProvisioningService(companyStore,
				new FakeEdgarClient(null), new EdgarCompanyProfileParser(new ObjectMapper()),
				new FakeFinancialDataProperties());
		service = new CompanyFilingsService(provisioning, refresher, secFilingRepository);
	}

	@Test
	void throwsCompanyNotFoundWhenCikUnknown() {
		assertThrows(CompanyNotFoundException.class, () -> service.getFilings("9999999999", null, FIRST_PAGE));
	}

	@Test
	void callsRefreshIfStaleBeforeReturningFilings() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		when(secFilingRepository.findByCompanyId(any(), any())).thenReturn(new PageImpl<>(List.of()));

		service.getFilings("0000320193", null, FIRST_PAGE);

		assertEquals(company, refresher.lastRefreshed());
	}

	@Test
	void returnsEmptyPageWhenNoFilingsStored() {
		companyStore.seed(new Company("0000320193", "AAPL", "Apple Inc."));
		when(secFilingRepository.findByCompanyId(any(), any())).thenReturn(new PageImpl<>(List.of()));

		PageResponse<SecFilingResponse> result = service.getFilings("0000320193", null, FIRST_PAGE);

		assertTrue(result.content().isEmpty());
	}

	@Test
	void returnsMappedFilingsForKnownCompany() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		SecFiling filing = new SecFiling(new SecFilingCreateRequest(company, "10-K", "2025-10-31", "aapl-20251231.htm",
				"Annual report", Instant.now()));
		when(secFilingRepository.findByCompanyId(any(), any())).thenReturn(new PageImpl<>(List.of(filing)));

		PageResponse<SecFilingResponse> result = service.getFilings("0000320193", null, FIRST_PAGE);

		assertEquals(1, result.content().size());
		assertEquals("10-K", result.content().get(0).formType());
		assertEquals("2025-10-31", result.content().get(0).filingDate());
		assertEquals("Annual report", result.content().get(0).description());
	}

	@Test
	void delegatesToSearchQueryWhenQueryProvided() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		companyStore.seed(company);
		SecFiling filing = new SecFiling(new SecFilingCreateRequest(company, "10-K", "2025-10-31", "aapl-20251231.htm",
				"Annual report", Instant.now()));
		when(secFilingRepository.searchByCompanyId(any(), eq("10-K"), any()))
				.thenReturn(new PageImpl<>(List.of(filing)));

		PageResponse<SecFilingResponse> result = service.getFilings("0000320193", "10-K", FIRST_PAGE);

		assertEquals(1, result.content().size());
		assertEquals("10-K", result.content().get(0).formType());
	}
}

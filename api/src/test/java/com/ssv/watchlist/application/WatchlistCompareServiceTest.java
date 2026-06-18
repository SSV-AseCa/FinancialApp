package com.ssv.watchlist.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;
import com.ssv.company.dto.CurrentCompanyMetrics;
import com.ssv.watchlist.dto.WatchlistCompareResponse;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class WatchlistCompareServiceTest {

	@Mock
	private WatchlistRepository watchlistRepository;

	@Mock
	private CompanyStore companyStore;

	@Mock
	private CompanyMetricsService companyMetricsService;

	private WatchlistCompareService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new WatchlistCompareService(watchlistRepository, companyStore, companyMetricsService);
	}

	@Test
	void throwsWhenCiksParamMissing() {
		UUID investorId = UUID.randomUUID();
		assertThrows(IllegalArgumentException.class, () -> service.compare(investorId, null));
		assertThrows(IllegalArgumentException.class, () -> service.compare(investorId, "   "));
	}

	@Test
	void throwsWhenFewerThanTwoCiksProvided() {
		UUID investorId = UUID.randomUUID();
		assertThrows(IllegalArgumentException.class, () -> service.compare(investorId, "0000320193"));
		assertThrows(IllegalArgumentException.class, () -> service.compare(investorId, "0000320193,  "));
	}

	@Test
	void throwsWhenCikNotOnWatchlist() {
		UUID investorId = UUID.randomUUID();
		Company c1 = companyWithCik("0000320193");
		Company c2 = companyWithCik("0000789019");

		when(companyStore.findByCik("0000320193")).thenReturn(Optional.of(c1));
		when(companyStore.findByCik("0000789019")).thenReturn(Optional.of(c2));

		when(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, c1.getId())).thenReturn(true);
		when(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, c2.getId())).thenReturn(false);

		// ensure metrics call won't return null if invoked unexpectedly
		when(companyMetricsService.currentMetrics(org.mockito.Mockito.anyString())).thenReturn(metrics(0, 0, 0, 0));

		assertThrows(IllegalArgumentException.class, () -> service.compare(investorId, "0000320193,0000789019"));
	}

	@Test
	void returnsComparisonForValidCiks() {
		UUID investorId = UUID.randomUUID();
		Company c1 = companyWithCik("0000320193");
		Company c2 = companyWithCik("0000789019");

		when(companyStore.findByCik("0000320193")).thenReturn(Optional.of(c1));
		when(companyStore.findByCik("0000789019")).thenReturn(Optional.of(c2));

		when(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, c1.getId())).thenReturn(true);
		when(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, c2.getId())).thenReturn(true);

		when(companyMetricsService.currentMetrics("0000320193")).thenReturn(metrics(1, 2, 3, 4));
		when(companyMetricsService.currentMetrics("0000789019")).thenReturn(metrics(10, 20, 30, 40));

		WatchlistCompareResponse result = service.compare(investorId, "0000320193,0000789019");

		assertEquals(2, result.companies().size());
		assertEquals("0000320193", result.companies().get(0).cik());
		assertEquals("0000789019", result.companies().get(1).cik());
		assertEquals(new BigDecimal("1"), result.companies().get(0).metrics().revenue());
		assertEquals(new BigDecimal("20"), result.companies().get(1).metrics().netIncome());
	}

	private Company companyWithCik(String cik) {
		Company company = new Company(cik, "SYM", "Name");
		ReflectionTestUtils.setField(company, "id", UUID.randomUUID());
		return company;
	}

	private CurrentCompanyMetrics metrics(int r, int n, int a, int e) {
		return new CurrentCompanyMetrics(new BigDecimal(String.valueOf(r)), new BigDecimal(String.valueOf(n)),
				new BigDecimal(String.valueOf(a)), new BigDecimal(String.valueOf(e)));
	}
}

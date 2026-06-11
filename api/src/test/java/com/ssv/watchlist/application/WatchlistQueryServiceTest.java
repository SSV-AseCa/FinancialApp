package com.ssv.watchlist.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;
import com.ssv.company.dto.CurrentCompanyMetrics;
import com.ssv.watchlist.domain.WatchlistEntry;
import com.ssv.watchlist.dto.CurrentFinancialMetrics;
import com.ssv.watchlist.dto.WatchlistCompanyResponse;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class WatchlistQueryServiceTest {

	@Mock
	private WatchlistRepository watchlistRepository;

	@Mock
	private CompanyStore companyStore;

	@Mock
	private CompanyMetricsService companyMetricsService;

	private WatchlistQueryService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		WatchlistCompanyMapper mapper = new WatchlistCompanyMapper(companyMetricsService);
		service = new WatchlistQueryService(watchlistRepository, companyStore, mapper);
	}

	@Test
	void returnsEmptyWhenNoEntries() {
		UUID investorId = UUID.randomUUID();
		when(watchlistRepository.findByInvestorId(investorId)).thenReturn(List.of());

		var result = service.getWatchlist(investorId);

		assertEquals(0, result.size());
	}

	@Test
	void returnsCompanyAndMetricsForEntries() {
		UUID investorId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		WatchlistEntry entry = entryFor(investorId, companyId);
		Company company = companyWithId(companyId);

		when(watchlistRepository.findByInvestorId(investorId)).thenReturn(List.of(entry));
		when(companyStore.findById(companyId)).thenReturn(Optional.of(company));
		when(companyMetricsService.currentMetrics("0000320193")).thenReturn(metrics());

		var result = service.getWatchlist(investorId);

		assertCompanyResponse(result.get(0), companyId);
	}

	private WatchlistEntry entryFor(UUID investorId, UUID companyId) {
		WatchlistEntry entry = new WatchlistEntry();
		entry.setId(UUID.randomUUID());
		entry.setInvestorId(investorId);
		entry.setCompanyId(companyId);
		return entry;
	}

	private Company companyWithId(UUID companyId) {
		Company company = new Company("0000320193", "AAPL", "Apple Inc");
		ReflectionTestUtils.setField(company, "id", companyId);
		return company;
	}

	private CurrentCompanyMetrics metrics() {
		return new CurrentCompanyMetrics(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
				new BigDecimal("4"));
	}

	private void assertCompanyResponse(WatchlistCompanyResponse response, UUID companyId) {
		assertEquals(companyId, response.companyId());
		assertEquals("0000320193", response.cik());
		CurrentFinancialMetrics metrics = response.metrics();
		assertEquals(new BigDecimal("1"), metrics.revenue());
		assertEquals(new BigDecimal("2"), metrics.netIncome());
	}
}

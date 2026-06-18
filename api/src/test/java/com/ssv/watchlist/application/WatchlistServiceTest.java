package com.ssv.watchlist.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;
import com.ssv.watchlist.domain.WatchlistEntry;
import com.ssv.watchlist.dto.AddWatchlistRequest;
import com.ssv.watchlist.dto.WatchlistResponse;
import com.ssv.watchlist.exceptions.DuplicateWatchlistEntryException;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;

class WatchlistServiceTest {

	@Mock
	private WatchlistRepository watchlistRepository;

	@Mock
	private CompanyStore companyStore;

	@Mock
	private CompanyProvisioningService companyProvisioningService;

	private WatchlistService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new WatchlistService(watchlistRepository, companyStore, companyProvisioningService);
	}

	@Test
	void addsToWatchlistSuccessfully() {
		UUID investorId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		Company company = new Company("0000320193", "AAPL", "Apple Inc");
		// ensure company has id via reflection or assume repository returns saved entry
		// id
		when(companyProvisioningService.ensureCompany("0000320193")).thenReturn(company);
		when(watchlistRepository.existsByInvestorIdAndCompanyId(any(), any())).thenReturn(false);
		WatchlistEntry saved = new WatchlistEntry();
		UUID entryId = UUID.randomUUID();
		saved.setId(entryId);
		saved.setCompanyId(companyId);
		saved.setInvestorId(investorId);
		when(watchlistRepository.save(any())).thenReturn(saved);

		WatchlistResponse resp = service.addToWatchlist(investorId, new AddWatchlistRequest("0000320193"));
		assertEquals(entryId, resp.id());
		assertEquals("0000320193", resp.cik());
	}

	@Test
	void throwsWhenCikIsInvalid() {
		UUID investorId = UUID.randomUUID();
		when(companyProvisioningService.ensureCompany("not-number"))
				.thenThrow(new IllegalArgumentException("Invalid CIK"));
		assertThrows(IllegalArgumentException.class,
				() -> service.addToWatchlist(investorId, new AddWatchlistRequest("not-number")));
	}

	@Test
	void throwsWhenDuplicate() {
		UUID investorId = UUID.randomUUID();
		Company company = new Company("0000320193", "AAPL", "Apple Inc");
		when(companyProvisioningService.ensureCompany("0000320193")).thenReturn(company);
		when(watchlistRepository.existsByInvestorIdAndCompanyId(any(), any())).thenReturn(true);

		assertThrows(DuplicateWatchlistEntryException.class,
				() -> service.addToWatchlist(investorId, new AddWatchlistRequest("0000320193")));
	}
}

package com.ssv.watchlist.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ssv.company.application.CompanyResearchService;
import com.ssv.company.application.CompanySearchService;
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
	private CompanySearchService companySearchService;

	@Mock
	private CompanyResearchService companyResearchService;

	private WatchlistService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new WatchlistService(watchlistRepository, companyStore, companySearchService, companyResearchService);
	}

	@Test
	void addsToWatchlistSuccessfully() {
		UUID investorId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		Company company = new Company("0000320193", "AAPL", "Apple Inc");

		when(companyStore.findByCik("0000320193")).thenReturn(Optional.of(company));
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
		assertThrows(IllegalArgumentException.class,
				() -> service.addToWatchlist(investorId, new AddWatchlistRequest("not-number")));
	}

	@Test
	void throwsWhenDuplicate() {
		UUID investorId = UUID.randomUUID();
		Company company = new Company("0000320193", "AAPL", "Apple Inc");
		when(companyStore.findByCik("0000320193")).thenReturn(Optional.of(company));
		when(watchlistRepository.existsByInvestorIdAndCompanyId(any(), any())).thenReturn(true);

		assertThrows(DuplicateWatchlistEntryException.class,
				() -> service.addToWatchlist(investorId, new AddWatchlistRequest("0000320193")));
	}

	@Test
	void removesFromWatchlistSuccessfully() {
		UUID investorId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		Company company = new Company("0000320193", "AAPL", "Apple Inc");
		org.springframework.test.util.ReflectionTestUtils.setField(company, "id", companyId);

		WatchlistEntry entry = new WatchlistEntry();
		entry.setId(UUID.randomUUID());
		entry.setInvestorId(investorId);
		entry.setCompanyId(companyId);

		when(companyStore.findByCik("0000320193")).thenReturn(Optional.of(company));
		when(watchlistRepository.findByInvestorIdAndCompanyId(investorId, companyId)).thenReturn(Optional.of(entry));

		service.removeFromWatchlist(investorId, "0000320193");

		org.mockito.Mockito.verify(watchlistRepository).delete(entry);
	}

	@Test
	void removeFromWatchlistThrowsWhenCikIsInvalid() {
		UUID investorId = UUID.randomUUID();
		assertThrows(IllegalArgumentException.class, () -> service.removeFromWatchlist(investorId, "not-number"));
	}

	@Test
	void removeFromWatchlistThrowsWhenCompanyNotFound() {
		UUID investorId = UUID.randomUUID();
		when(companyStore.findByCik("0000320193")).thenReturn(Optional.empty());

		assertThrows(com.ssv.watchlist.exceptions.WatchlistEntryNotFoundException.class,
				() -> service.removeFromWatchlist(investorId, "0000320193"));
	}

	@Test
	void removeFromWatchlistThrowsWhenEntryNotFound() {
		UUID investorId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		Company company = new Company("0000320193", "AAPL", "Apple Inc");
		org.springframework.test.util.ReflectionTestUtils.setField(company, "id", companyId);

		when(companyStore.findByCik("0000320193")).thenReturn(Optional.of(company));
		when(watchlistRepository.findByInvestorIdAndCompanyId(investorId, companyId)).thenReturn(Optional.empty());

		assertThrows(com.ssv.watchlist.exceptions.WatchlistEntryNotFoundException.class,
				() -> service.removeFromWatchlist(investorId, "0000320193"));
	}
}

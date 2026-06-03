package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.ssv.company.domain.Company;

class CompanyResearchServiceTest {

	@Test
	void shouldNormalizeCikAndCreateCompanyWhenMissing() {
		CompanyStore store = mock(CompanyStore.class);
		CompanyFinancialDataRefresher refresher = mock(CompanyFinancialDataRefresher.class);
		Company saved = company();
		when(store.findByCik("0000320193")).thenReturn(Optional.empty());
		when(store.save(any(Company.class))).thenReturn(saved);

		CompanyFinancialData data = service(store, refresher).getOrFetchFinancialData(request());

		assertEquals("0000320193", data.company().getCik());
		assertEquals("AAPL", data.company().getSymbol());
		verify(refresher).refreshIfStale(saved);
	}

	@Test
	void shouldUseExistingCompanyWhenPresent() {
		CompanyStore store = mock(CompanyStore.class);
		CompanyFinancialDataRefresher refresher = mock(CompanyFinancialDataRefresher.class);
		Company existing = company();
		when(store.findByCik("0000320193")).thenReturn(Optional.of(existing));

		CompanyFinancialData data = service(store, refresher).getOrFetchFinancialData(request());

		assertEquals(existing, data.company());
		verify(store, never()).save(any(Company.class));
		verify(refresher).refreshIfStale(existing);
	}

	@Test
	void shouldReloadCompanyWhenConcurrentInsertWinsRace() {
		CompanyStore store = mock(CompanyStore.class);
		CompanyFinancialDataRefresher refresher = mock(CompanyFinancialDataRefresher.class);
		Company existing = company();
		when(store.findByCik("0000320193")).thenReturn(Optional.empty(), Optional.of(existing));
		when(store.save(any(Company.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

		CompanyFinancialData data = service(store, refresher).getOrFetchFinancialData(request());

		assertEquals(existing, data.company());
		verify(refresher).refreshIfStale(existing);
	}

	private CompanyResearchService service(CompanyStore store, CompanyFinancialDataRefresher refresher) {
		return new CompanyResearchService(store, refresher);
	}

	private CompanyRequest request() {
		return new CompanyRequest("320193", " AAPL ", " Apple Inc. ");
	}

	private Company company() {
		return new Company("0000320193", "AAPL", "Apple Inc.");
	}
}

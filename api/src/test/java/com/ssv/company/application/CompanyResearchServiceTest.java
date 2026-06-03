package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssv.company.domain.Company;
import com.ssv.company.infrastructure.persistence.CompanyRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CompanyResearchServiceTest {

	@Test
	void shouldNormalizeCikAndCreateCompanyWhenMissing() {
		CompanyRepository repository = mock(CompanyRepository.class);
		CompanyFinancialDataRefresher refresher = mock(CompanyFinancialDataRefresher.class);
		Company saved = company();
		when(repository.findByCik("0000320193")).thenReturn(Optional.empty());
		when(repository.save(any(Company.class))).thenReturn(saved);

		CompanyFinancialData data = service(repository, refresher).getOrFetchFinancialData(request());

		assertEquals("0000320193", data.company().getCik());
		assertEquals("AAPL", data.company().getSymbol());
		verify(refresher).refreshIfStale(saved);
	}

	@Test
	void shouldUseExistingCompanyWhenPresent() {
		CompanyRepository repository = mock(CompanyRepository.class);
		CompanyFinancialDataRefresher refresher = mock(CompanyFinancialDataRefresher.class);
		Company existing = company();
		when(repository.findByCik("0000320193")).thenReturn(Optional.of(existing));

		CompanyFinancialData data = service(repository, refresher).getOrFetchFinancialData(request());

		assertEquals(existing, data.company());
		verify(repository, never()).save(any(Company.class));
		verify(refresher).refreshIfStale(existing);
	}

	private CompanyResearchService service(CompanyRepository repository, CompanyFinancialDataRefresher refresher) {
		return new CompanyResearchService(repository, refresher);
	}

	private CompanyRequest request() {
		return new CompanyRequest("320193", " AAPL ", " Apple Inc. ");
	}

	private Company company() {
		return new Company("0000320193", "AAPL", "Apple Inc.");
	}
}

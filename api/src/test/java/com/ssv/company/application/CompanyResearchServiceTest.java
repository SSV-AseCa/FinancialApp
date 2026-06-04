package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.ssv.company.application.fake.FakeCompanyFinancialDataRefresher;
import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.company.domain.Company;

class CompanyResearchServiceTest {

	@Test
	void shouldNormalizeCikAndCreateCompanyWhenMissing() {
		FakeCompanyStore store = new FakeCompanyStore();
		FakeCompanyFinancialDataRefresher refresher = new FakeCompanyFinancialDataRefresher();

		CompanyFinancialData data = service(store, refresher).getOrFetchFinancialData(request());

		assertEquals("0000320193", data.company().getCik());
		assertEquals("AAPL", data.company().getSymbol());
		assertEquals(data.company(), refresher.lastRefreshed());
	}

	@Test
	void shouldUseExistingCompanyWhenPresent() {
		FakeCompanyStore store = new FakeCompanyStore();
		Company existing = company();
		store.seed(existing);
		FakeCompanyFinancialDataRefresher refresher = new FakeCompanyFinancialDataRefresher();

		CompanyFinancialData data = service(store, refresher).getOrFetchFinancialData(request());

		assertEquals(existing, data.company());
		assertEquals(existing, refresher.lastRefreshed());
	}

	@Test
	void shouldReloadCompanyWhenConcurrentInsertWinsRace() {
		FakeCompanyStore store = new FakeCompanyStore();
		Company existing = company();
		store.firstFindReturnsEmpty();
		store.seed(existing);
		store.throwOnNextSave(new DataIntegrityViolationException("duplicate"));
		FakeCompanyFinancialDataRefresher refresher = new FakeCompanyFinancialDataRefresher();

		CompanyFinancialData data = service(store, refresher).getOrFetchFinancialData(request());

		assertEquals(existing, data.company());
		assertEquals(existing, refresher.lastRefreshed());
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

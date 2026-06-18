package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.company.application.fake.FakeFinancialStatementStore;
import com.ssv.company.application.fake.FakeSecFilingStore;
import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.FinancialStatementCreateRequest;
import org.junit.jupiter.api.Test;

class FinancialDataPersisterTest {

	private static final Instant FETCHED_AT = Instant.parse("2026-06-01T00:00:00Z");

	@Test
	void replaceDeletesThenSavesAndStampsFreshness() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		FakeFinancialStatementStore statementStore = new FakeFinancialStatementStore();
		FakeSecFilingStore filingStore = new FakeSecFilingStore();
		FakeCompanyStore companyStore = new FakeCompanyStore();
		FinancialDataPersister persister = new FinancialDataPersister(statementStore, filingStore, companyStore);
		FinancialStatement statement = new FinancialStatement(new FinancialStatementCreateRequest(company, "Revenue",
				new BigDecimal("394328000000"), "USD", "2023-09-30", FETCHED_AT));

		persister.replace(company, List.of(statement), List.of(), FETCHED_AT);

		assertTrue(statementStore.wasDeleteCalled());
		assertTrue(filingStore.wasDeleteCalled());
		assertEquals(List.of(statement), statementStore.saved());
		assertEquals(FETCHED_AT, company.getFinancialsFetchedAt());
		assertEquals(company, companyStore.findByCik("0000320193").orElseThrow());
	}
}

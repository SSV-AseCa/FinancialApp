package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.application.fake.FakeEdgarCompanyFactsParser;
import com.ssv.company.application.fake.FakeEdgarCompanyFilingsParser;
import com.ssv.company.application.fake.FakeFinancialStatementStore;
import com.ssv.company.application.fake.FakeSecFilingStore;
import com.ssv.company.domain.Company;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CompanyFinancialDataRefresherTest {

	private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	@Test
	void fetchCompanySubmissionsStripsCikBuildsPathAndReturnsResponse() {
		FakeEdgarClient edgarClient = new FakeEdgarClient("company-data");
		FakeFinancialStatementStore statementStore = new FakeFinancialStatementStore();
		FakeSecFilingStore filingStore = new FakeSecFilingStore();
		CompanyFinancialDataRefresher refresher = refresher(edgarClient, statementStore, filingStore);

		String response = refresher.fetchCompanySubmissions("  320193  ");

		assertEquals("company-data", response);
		assertEquals("/submissions/CIK320193.json", edgarClient.receivedPath());
	}

	@Test
	void refreshIfStaleReturnsFalseWhenDataIsFresh() {
		Company company = companyFetchedAt(NOW.minusSeconds(100));
		FakeFinancialStatementStore statementStore = new FakeFinancialStatementStore();
		FakeSecFilingStore filingStore = new FakeSecFilingStore();
		CompanyFinancialDataRefresher refresher = refresher(new FakeEdgarClient(""), statementStore, filingStore);

		boolean refreshed = refresher.refreshIfStale(company);

		assertFalse(refreshed);
		assertFalse(statementStore.wasDeleteCalled());
	}

	@Test
	void refreshIfStaleReturnsTrueWhenDataIsStale() {
		Company company = companyFetchedAt(NOW.minusSeconds(200000));
		FakeFinancialStatementStore statementStore = new FakeFinancialStatementStore();
		FakeSecFilingStore filingStore = new FakeSecFilingStore();

		boolean refreshed = refresher(new FakeEdgarClient("{}"), statementStore, filingStore).refreshIfStale(company);

		assertTrue(refreshed);
		assertEquals(NOW, company.getFinancialsFetchedAt());
	}

	@Test
	void refreshIfStaleReturnsTrueWhenFetchedAtIsNull() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		FakeFinancialStatementStore statementStore = new FakeFinancialStatementStore();
		FakeSecFilingStore filingStore = new FakeSecFilingStore();

		boolean refreshed = refresher(new FakeEdgarClient("{}"), statementStore, filingStore).refreshIfStale(company);

		assertTrue(refreshed);
		assertEquals(NOW, company.getFinancialsFetchedAt());
	}

	private CompanyFinancialDataRefresher refresher(FakeEdgarClient client, FakeFinancialStatementStore statementStore,
			FakeSecFilingStore filingStore) {
		return new CompanyFinancialDataRefresher(properties(), client, new FakeEdgarCompanyFactsParser(),
				new FakeEdgarCompanyFilingsParser(), statementStore, filingStore, null, null, CLOCK);
	}

	private FinancialDataProperties properties() {
		return new FinancialDataProperties() {
			@Override
			public String submissionsPath() {
				return "/submissions/CIK%s.json";
			}

			@Override
			public String companyFactsPath() {
				return "/api/xbrl/companyfacts/CIK%s.json";
			}

			@Override
			public int stalenessDays() {
				return 1;
			}
		};
	}

	private Company companyFetchedAt(Instant fetchedAt) {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		company.markFinancialsFetched(fetchedAt);
		return company;
	}
}

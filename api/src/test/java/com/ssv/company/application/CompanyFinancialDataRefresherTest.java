package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.application.fake.FakeEdgarCompanyFactsParser;
import com.ssv.company.application.fake.FakeEdgarCompanyFilingsParser;
import com.ssv.company.application.fake.FakeFinancialDataPersister;
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
		CompanyFinancialDataRefresher refresher = refresher(edgarClient, new FakeFinancialDataPersister());

		String response = refresher.fetchCompanySubmissions("  320193  ");

		assertEquals("company-data", response);
		assertEquals("/submissions/CIK320193.json", edgarClient.receivedPath());
	}

	@Test
	void refreshIfStaleReturnsFalseWhenDataIsFresh() {
		Company company = companyFetchedAt(NOW.minusSeconds(100));
		FakeFinancialDataPersister persister = new FakeFinancialDataPersister();
		CompanyFinancialDataRefresher refresher = refresher(new FakeEdgarClient(""), persister);

		boolean refreshed = refresher.refreshIfStale(company);

		assertFalse(refreshed);
		assertFalse(persister.wasReplaceCalled());
	}

	@Test
	void refreshIfStaleDelegatesToPersisterWhenDataIsStale() {
		Company company = companyFetchedAt(NOW.minusSeconds(200000));
		FakeFinancialDataPersister persister = new FakeFinancialDataPersister();

		boolean refreshed = refresher(new FakeEdgarClient("{}"), persister).refreshIfStale(company);

		assertTrue(refreshed);
		assertTrue(persister.wasReplaceCalled());
		assertEquals(company, persister.lastCompany());
		assertEquals(NOW, persister.lastFetchedAt());
	}

	@Test
	void refreshIfStaleDelegatesToPersisterWhenFetchedAtIsNull() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		FakeFinancialDataPersister persister = new FakeFinancialDataPersister();

		boolean refreshed = refresher(new FakeEdgarClient("{}"), persister).refreshIfStale(company);

		assertTrue(refreshed);
		assertTrue(persister.wasReplaceCalled());
		assertEquals(NOW, persister.lastFetchedAt());
	}

	private CompanyFinancialDataRefresher refresher(FakeEdgarClient client, FakeFinancialDataPersister persister) {
		return new CompanyFinancialDataRefresher(properties(), client, new FakeEdgarCompanyFactsParser(),
				new FakeEdgarCompanyFilingsParser(), null, null, persister, CLOCK);
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

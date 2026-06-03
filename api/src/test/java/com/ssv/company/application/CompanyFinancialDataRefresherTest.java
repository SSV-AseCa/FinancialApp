package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.ssv.company.application.EdgarClient.FakeEdgarClient;
import com.ssv.company.domain.Company;
import com.ssv.edgar.infrastructure.config.EdgarProperties;
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
		CompanyFinancialDataRefresher refresher = refresher(edgarClient);

		String response = refresher.fetchCompanySubmissions("  320193  ");

		assertEquals("company-data", response);
		assertEquals("/submissions/CIK320193.json", edgarClient.receivedPath());
	}

	@Test
	void refreshIfStaleReturnsFalseWhenDataIsFresh() {
		Company company = companyFetchedAt(NOW.minusSeconds(100));

		boolean refreshed = refresher(new FakeEdgarClient("")).refreshIfStale(company);

		assertFalse(refreshed);
	}

	@Test
	void refreshIfStaleReturnsTrueWhenDataIsStale() {
		Company company = companyFetchedAt(NOW.minusSeconds(200000));

		boolean refreshed = refresher(new FakeEdgarClient("")).refreshIfStale(company);

		assertTrue(refreshed);
		assertEquals(NOW, company.getFinancialsFetchedAt());
	}

	@Test
	void refreshIfStaleReturnsTrueWhenFetchedAtIsNull() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");

		boolean refreshed = refresher(new FakeEdgarClient("")).refreshIfStale(company);

		assertTrue(refreshed);
		assertEquals(NOW, company.getFinancialsFetchedAt());
	}

	private CompanyFinancialDataRefresher refresher(FakeEdgarClient client) {
		return new CompanyFinancialDataRefresher(properties(), client, CLOCK);
	}

	private EdgarProperties properties() {
		return new EdgarProperties("https://data.sec.gov", "test-user-agent", "", "/submissions/CIK%s.json",
				"/api/xbrl/companyfacts/CIK%s.json", 1, "https://efts.sec.gov", "/LATEST/search-index",
				new EdgarProperties.RateLimit(10, 1000));
	}

	private Company companyFetchedAt(Instant fetchedAt) {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		company.markFinancialsFetched(fetchedAt);
		return company;
	}
}

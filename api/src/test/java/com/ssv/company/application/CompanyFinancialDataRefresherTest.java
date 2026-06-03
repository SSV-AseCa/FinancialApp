package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.domain.Company;
import com.ssv.edgar.application.EdgarCompanyFactsParser;
import com.ssv.edgar.application.EdgarCompanyFilingsParser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompanyFinancialDataRefresherTest {

	private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	@Test
	void fetchCompanySubmissionsStripsCikBuildsPathAndReturnsResponse() {
		FakeEdgarClient edgarClient = new FakeEdgarClient("company-data");
		TestRefresher refresher = refresher(edgarClient);

		String response = refresher.fetchCompanySubmissions("  320193  ");

		assertEquals("company-data", response);
		assertEquals("/submissions/CIK320193.json", edgarClient.receivedPath());
	}

	@Test
	void refreshIfStaleReturnsFalseWhenDataIsFresh() {
		Company company = companyFetchedAt(NOW.minusSeconds(100));
		TestRefresher refresher = refresher(new FakeEdgarClient(""));

		boolean refreshed = refresher.refreshIfStale(company);

		assertFalse(refreshed);
		verify(refresher.financialStatementStore(), never()).deleteByCompanyId(any());
	}

	@Test
	void refreshIfStaleReturnsTrueWhenDataIsStale() {
		Company company = companyFetchedAt(NOW.minusSeconds(200000));

		boolean refreshed = refresher(new FakeEdgarClient("{}")).refreshIfStale(company);

		assertTrue(refreshed);
		assertEquals(NOW, company.getFinancialsFetchedAt());
	}

	@Test
	void refreshIfStaleReturnsTrueWhenFetchedAtIsNull() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");

		boolean refreshed = refresher(new FakeEdgarClient("{}")).refreshIfStale(company);

		assertTrue(refreshed);
		assertEquals(NOW, company.getFinancialsFetchedAt());
	}

	private TestRefresher refresher(FakeEdgarClient client) {
		EdgarCompanyFactsParser factsParser = mock(EdgarCompanyFactsParser.class);
		EdgarCompanyFilingsParser filingsParser = mock(EdgarCompanyFilingsParser.class);
		FinancialStatementStore statementStore = mock(FinancialStatementStore.class);
		SecFilingStore filingStore = mock(SecFilingStore.class);
		FinancialStatementFactory statementFactory = mock(FinancialStatementFactory.class);
		SecFilingFactory filingFactory = mock(SecFilingFactory.class);

		when(factsParser.parse(any())).thenReturn(List.of());
		when(filingsParser.parse(any())).thenReturn(List.of());

		CompanyFinancialDataRefresher refresher = new CompanyFinancialDataRefresher(properties(), client, factsParser,
				filingsParser, statementStore, filingStore, statementFactory, filingFactory, CLOCK);

		return new TestRefresher(refresher, statementStore, filingStore);
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

	private record TestRefresher(CompanyFinancialDataRefresher refresher,
			FinancialStatementStore financialStatementStore, SecFilingStore secFilingStore) {
		boolean refreshIfStale(Company company) {
			return refresher.refreshIfStale(company);
		}

		String fetchCompanySubmissions(String cik) {
			return refresher.fetchCompanySubmissions(cik);
		}
	}
}

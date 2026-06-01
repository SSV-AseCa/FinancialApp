package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ssv.company.application.EdgarClient.FakeEdgarClient;
import com.ssv.edgar.infrastructure.config.EdgarProperties;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class CompanyFinancialDataRefresherTest {

	@Test
	void fetchCompanySubmissionsStripsCikBuildsPathAndReturnsResponse() {
		FakeEdgarClient edgarClient = new FakeEdgarClient("company-data");

		EdgarProperties properties = new EdgarProperties("https://data.sec.gov", "test-user-agent", "",
				"/submissions/CIK%s.json", "/api/xbrl/companyfacts/CIK%s.json", 1,
				new EdgarProperties.RateLimit(10, 1000));

		CompanyFinancialDataRefresher refresher = new CompanyFinancialDataRefresher(properties, edgarClient,
				Clock.systemUTC());

		String response = refresher.fetchCompanySubmissions("  320193  ");

		assertEquals("company-data", response);
		assertEquals("/submissions/CIK320193.json", edgarClient.receivedPath());
	}
}

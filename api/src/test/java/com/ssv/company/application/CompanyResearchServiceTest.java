package com.ssv.company.application;

import com.ssv.company.application.EdgarClient.FakeEdgarClient;
import com.ssv.edgar.infrastructure.config.EdgarProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompanyResearchServiceTest {

	@Test
	void fetchCompanySubmissionsStripsCikBuildsPathAndReturnsResponse() {
		FakeEdgarClient edgarClient = new FakeEdgarClient("company-data");

		EdgarProperties properties = new EdgarProperties("https://data.sec.gov", "test-user-agent",
				"/submissions/CIK%s.json", new EdgarProperties.RateLimit(10, 1000));

		CompanyResearchService service = new CompanyResearchService(edgarClient, properties);

		String response = service.fetchCompanySubmissions("  320193  ");

		assertEquals("company-data", response);
		assertEquals("/submissions/CIK320193.json", edgarClient.receivedPath());
	}
}

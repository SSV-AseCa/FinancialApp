package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.edgar.application.EdgarCompanyFactsParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class CompanyMetricsServiceTest {

	@Test
	void callsEdgarClientWithCompanyCikPath() {
		FakeEdgarClient client = new FakeEdgarClient("{\"facts\":{}}\n");
		FinancialDataProperties props = new FinancialDataProperties() {
			@Override
			public String submissionsPath() {
				return "/submissions/%s";
			}

			@Override
			public String companyFactsPath() {
				return "/company/%s/facts";
			}

			@Override
			public int stalenessDays() {
				return 7;
			}
		};

		EdgarCompanyFactsParser parser = new EdgarCompanyFactsParser(new ObjectMapper());
		CompanyMetricsService svc = new CompanyMetricsService(props, client, parser);

		svc.currentMetrics("0000320193");

		assertEquals("/company/0000320193/facts", client.receivedPath());
	}
}

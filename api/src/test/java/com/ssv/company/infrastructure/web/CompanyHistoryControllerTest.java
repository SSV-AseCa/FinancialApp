package com.ssv.company.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.FinancialDataProperties;
import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.domain.Company;
import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.edgar.application.EdgarCompanyFactsParser;

import java.util.List;

@WebMvcTest(CompanyController.class)
@Import(CompanyHistoryControllerTest.Config.class)
@TestPropertySource(properties = { "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com" })
class CompanyHistoryControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakeCompanyStore companyStore() {
			return new FakeCompanyStore();
		}

		@Bean
		FakeEdgarClient edgarClient() {
			return new FakeEdgarClient("{}");
		}

		@Bean
		EdgarCompanyFactsParser factsParser() {
			return new EdgarCompanyFactsParser(new ObjectMapper());
		}

		@Bean
		FinancialDataProperties financialDataProperties() {
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
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeCompanyStore companyStore;

	@Autowired
	private FakeEdgarClient edgarClient;

	@BeforeEach
	void reset() {
		companyStore = companyStore; // no-op to ensure it's injected
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/companies/0000320193/history")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns404WhenUnknownCik() throws Exception {
		mockMvc.perform(get("/companies/0000000000/history").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isNotFound());
	}

	@Test
	void returnsHistoryWhenAuthenticated() throws Exception {
		// seed company
		companyStore.seed(new Company("0000320193", "AAPL", "Apple Inc."));

		String facts = "{" +
			"\"facts\":{\"us-gaap\":{" +
			"\"Assets\":{\"units\":{\"USD\":[{\"val\":987654321,\"end\":\"2022-12-31\"},{\"val\":1087654321,\"end\":\"2023-12-31\"}]}}," +
			"\"Revenues\":{\"units\":{\"USD\":[{\"val\":123456789,\"end\":\"2022-12-31\"},{\"val\":223456789,\"end\":\"2023-12-31\"}]}}," +
			"\"NetIncomeLoss\":{\"units\":{\"USD\":[{\"val\":12345678,\"end\":\"2022-12-31\"},{\"val\":22345678,\"end\":\"2023-12-31\"}]}}" +
			"}}}";

		edgarClient.setResponse(facts);

		mockMvc.perform(get("/companies/0000320193/history").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].period").value("2022"))
				.andExpect(jsonPath("$[0].revenue").value(123456789))
				.andExpect(jsonPath("$[1].period").value("2023"))
				.andExpect(jsonPath("$[1].revenue").value(223456789));
	}

	@Test
	void usesEdgarClientPath() throws Exception {
		companyStore.seed(new Company("0000320193", "AAPL", "Apple Inc."));
		edgarClient.setResponse("{}");
		mockMvc.perform(get("/companies/0000320193/history").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk());
		// assert path used
		org.assertj.core.api.Assertions.assertThat(edgarClient.receivedPath()).isEqualTo("/api/xbrl/companyfacts/CIK0000320193.json");
	}
}

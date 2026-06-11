package com.ssv.company.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.company.application.fake.FakeCompanySearchService;
import com.ssv.company.dto.CompanySearchResult;

@WebMvcTest(CompanyController.class)
@Import(CompanyControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class CompanyControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakeCompanySearchService companySearchService() {
			return new FakeCompanySearchService();
		}

		@Bean
		com.ssv.company.application.CompanyFilingsService companyFilingsService() {
			// simple stub for controller unit tests that don't exercise filings
			com.ssv.company.application.FinancialDataProperties props = new com.ssv.company.application.FinancialDataProperties() {
				@Override
				public String submissionsPath() {
					return "/submissions/%s";
				}

				@Override
				public String companyFactsPath() {
					return "/facts/%s";
				}

				@Override
				public int stalenessDays() {
					return 7;
				}
			};
			com.ssv.company.application.fake.FakeCompanyStore store = new com.ssv.company.application.fake.FakeCompanyStore();
			return new com.ssv.company.application.CompanyFilingsService(store, props,
					(path) -> "{\"filings\":{\"recent\":{\"form\":[],\"filingDate\":[],\"primaryDocument\":[]}}}",
					new com.ssv.edgar.application.EdgarCompanyFilingsParser(
							new com.fasterxml.jackson.databind.ObjectMapper()));
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeCompanySearchService companySearchService;

	@BeforeEach
	void reset() {
		companySearchService.reset();
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/companies/search").param("q", "apple")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenQParamIsMissing() throws Exception {
		mockMvc.perform(get("/companies/search").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns400WhenQParamIsBlank() throws Exception {
		mockMvc.perform(get("/companies/search").param("q", "   ").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns200WithResultsForValidQuery() throws Exception {
		companySearchService.respondWith("apple",
				List.of(new CompanySearchResult("Apple Inc.", "0000320193", List.of("AAPL"))));

		mockMvc.perform(get("/companies/search").param("q", "apple").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Apple Inc."))
				.andExpect(jsonPath("$[0].cik").value("0000320193"));
	}

	@Test
	void returns200WithEmptyListWhenNoResults() throws Exception {
		mockMvc.perform(
				get("/companies/search").param("q", "unknownxyz").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void stripsWhitespaceFromQueryBeforeDelegating() throws Exception {
		companySearchService.respondWith("apple", List.of());

		mockMvc.perform(
				get("/companies/search").param("q", "  apple  ").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk());
	}
}

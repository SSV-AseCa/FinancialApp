package com.ssv.company.infrastructure.web;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.ssv.company.application.CompanyHistoryService;
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

import com.ssv.company.application.fake.FakeCompanyFilingsService;
import com.ssv.company.application.fake.FakeCompanyMetricsService;
import com.ssv.company.application.fake.FakeCompanySearchService;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.dto.SecFilingResponse;

import java.math.BigDecimal;

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
		CompanyHistoryService companyHistoryService() {
			return mock(CompanyHistoryService.class);
		}

		@Bean
		FakeCompanyMetricsService companyMetricsService() {
			return new FakeCompanyMetricsService();
		}

		@Bean
		FakeCompanyFilingsService companyFilingsService() {
			return new FakeCompanyFilingsService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeCompanySearchService companySearchService;

	@Autowired
	private FakeCompanyMetricsService companyMetricsService;

	@Autowired
	private FakeCompanyFilingsService companyFilingsService;

	@BeforeEach
	void reset() {
		companySearchService.reset();
		companyMetricsService.reset();
		companyFilingsService.reset();
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

	@Test
	void metricsReturns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/companies/0000320193/metrics")).andExpect(status().isUnauthorized());
	}

	@Test
	void metricsReturns404WhenCikUnknown() throws Exception {
		companyMetricsService.notFoundFor("9999999999");

		mockMvc.perform(get("/companies/9999999999/metrics").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isNotFound());
	}

	@Test
	void metricsReturns200WithMetrics() throws Exception {
		companyMetricsService.respondWith("0000320193",
				List.of(new FinancialMetricResponse("Revenue", new BigDecimal("394328000000"), "USD", "2023-09-30")));

		mockMvc.perform(get("/companies/0000320193/metrics").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].metric").value("Revenue"))
				.andExpect(jsonPath("$.content[0].value").value(394328000000.0))
				.andExpect(jsonPath("$.content[0].unit").value("USD"))
				.andExpect(jsonPath("$.content[0].periodEnd").value("2023-09-30"))
				.andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.page").value(0));
	}

	@Test
	void metricsReturns200WithEmptyPageWhenNoMetricsStored() throws Exception {
		mockMvc.perform(get("/companies/0000320193/metrics").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content").isEmpty()).andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void metricsFiltersByQueryParam() throws Exception {
		companyMetricsService.respondWith("0000320193",
				List.of(new FinancialMetricResponse("Revenues", new BigDecimal("1"), "USD", "2023-09-30"),
						new FinancialMetricResponse("Assets", new BigDecimal("2"), "USD", "2023-09-30")));

		mockMvc.perform(
				get("/companies/0000320193/metrics").param("q", "rev").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].metric").value("Revenues"));
	}

	@Test
	void metricsHonoursPageAndSizeParams() throws Exception {
		companyMetricsService.respondWith("0000320193",
				List.of(new FinancialMetricResponse("A", new BigDecimal("1"), "USD", "2023-09-30"),
						new FinancialMetricResponse("B", new BigDecimal("2"), "USD", "2023-09-30"),
						new FinancialMetricResponse("C", new BigDecimal("3"), "USD", "2023-09-30")));

		mockMvc.perform(get("/companies/0000320193/metrics").param("page", "1").param("size", "2")
				.with(SecurityMockMvcRequestPostProcessors.jwt())).andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(1)).andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.content.length()").value(1));
	}

	@Test
	void filingsReturns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings")).andExpect(status().isUnauthorized());
	}

	@Test
	void filingsReturns404WhenCikUnknown() throws Exception {
		companyFilingsService.notFoundFor("9999999999");

		mockMvc.perform(get("/companies/9999999999/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isNotFound());
	}

	@Test
	void filingsReturns200WithFilings() throws Exception {
		companyFilingsService.respondWith("0000320193",
				List.of(new SecFilingResponse("10-K", "2025-10-31", "Annual report")));

		mockMvc.perform(get("/companies/0000320193/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].formType").value("10-K"))
				.andExpect(jsonPath("$.content[0].filingDate").value("2025-10-31"))
				.andExpect(jsonPath("$.content[0].description").value("Annual report"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void filingsReturns200WithEmptyPageWhenNoFilingsStored() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content").isEmpty()).andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void filingsFiltersByQueryParam() throws Exception {
		companyFilingsService.respondWith("0000320193",
				List.of(new SecFilingResponse("10-K", "2025-10-31", "Annual report"),
						new SecFilingResponse("8-K", "2025-09-01", "Current report")));

		mockMvc.perform(get("/companies/0000320193/filings").param("q", "annual")
				.with(SecurityMockMvcRequestPostProcessors.jwt())).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].formType").value("10-K"));
	}
}

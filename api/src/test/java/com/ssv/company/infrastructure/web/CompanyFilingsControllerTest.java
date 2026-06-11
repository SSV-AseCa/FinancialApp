package com.ssv.company.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.ssv.company.application.fake.FakeCompanyStore;
import com.ssv.company.domain.Company;

@WebMvcTest(CompanyController.class)
@Import(CompanyFilingsControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class CompanyFilingsControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakeCompanyStore companyStore() {
			return new FakeCompanyStore();
		}

		@Bean
		com.ssv.company.application.fake.FakeCompanySearchService companySearchService() {
			return new com.ssv.company.application.fake.FakeCompanySearchService();
		}

		@Bean
		com.ssv.company.application.FinancialDataProperties properties() {
			return new com.ssv.company.application.FinancialDataProperties() {
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
		}

		@Bean
		com.ssv.edgar.application.EdgarClient edgarClient() {
			return new com.ssv.edgar.application.EdgarClient() {
				private String lastPath;

				@Override
				public String get(String path) {
					this.lastPath = path;
					int count = 12; // produce multiple filings to test limit
					StringBuilder forms = new StringBuilder();
					StringBuilder dates = new StringBuilder();
					StringBuilder docs = new StringBuilder();
					forms.append("[");
					dates.append("[");
					docs.append("[");
					for (int i = 0; i < count; i++) {
						if (i > 0) {
							forms.append(",");
							dates.append(",");
							docs.append(",");
						}
						String form = (i == 0) ? "10-K" : (i == 1) ? "8-K" : "F-" + i;
						forms.append('"').append(form).append('"');
						// descending dates using YearMonth for correctness
						java.time.YearMonth ym = java.time.YearMonth.of(2026, 6).minusMonths(i);
						String date = ym.toString() + "-01";
						dates.append('"').append(date).append('"');
						docs.append('"').append("doc" + i + ".htm").append('"');
					}
					forms.append("]");
					dates.append("]");
					docs.append("]");
					return "{\"filings\":{\"recent\":{\"form\":" + forms.toString() + ",\"filingDate\":"
							+ dates.toString() + ",\"primaryDocument\":" + docs.toString() + "}}}";
				}

				public String lastPath() {
					return lastPath;
				}
			};
		}

		@Bean
		com.ssv.edgar.application.EdgarCompanyFilingsParser filingsParser() {
			return new com.ssv.edgar.application.EdgarCompanyFilingsParser(
					new com.fasterxml.jackson.databind.ObjectMapper());
		}

		@Bean
		com.ssv.company.application.CompanyFilingsService companyFilingsService(FakeCompanyStore store,
				com.ssv.company.application.FinancialDataProperties props,
				com.ssv.edgar.application.EdgarClient edgarClient,
				com.ssv.edgar.application.EdgarCompanyFilingsParser parser) {
			return new com.ssv.company.application.CompanyFilingsService(store, props, edgarClient, parser);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeCompanyStore companyStore;

	@Autowired
	private com.ssv.edgar.application.EdgarClient edgarClient;

	@BeforeEach
	void reset() {
		companyStore.seed(new Company("0000320193", "AAPL", "Apple Inc."));
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns404WhenCompanyUnknown() throws Exception {
		mockMvc.perform(get("/companies/0000000000/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isNotFound());
	}

	@Test
	void returnsFilingsForKnownCompanyOrderedByDate() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].formType").value("10-K"))
				.andExpect(jsonPath("$[0].filingDate").value("2026-06-01"))
				.andExpect(jsonPath("$[0].title").isNotEmpty()).andExpect(jsonPath("$[1].formType").value("8-K"));
	}

	@Test
	void edgarPathIsUsedThroughClient() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk());
		try {
			String last = (String) edgarClient.getClass().getMethod("lastPath").invoke(edgarClient);
			assertThat(last).isEqualTo("/submissions/0000320193");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void returnsAtMostDefaultLimit() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(10));
	}

	@Test
	void respectsLimitQueryParam() throws Exception {
		mockMvc.perform(get("/companies/0000320193/filings").param("limit", "1")
				.with(SecurityMockMvcRequestPostProcessors.jwt())).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].formType").value("10-K"));
	}
}

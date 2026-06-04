package com.ssv.company.infrastructure.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.company.application.CompanyResearchService;
import com.ssv.company.application.CompanySearchService;
import com.ssv.company.application.FinancialStatementStore;
import com.ssv.company.application.SecFilingStore;
import com.ssv.company.dto.CompanySearchResult;

@WebMvcTest(CompanyController.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class CompanyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CompanySearchService companySearchService;

	@MockitoBean
	private CompanyResearchService companyResearchService;

	@MockitoBean
	private FinancialStatementStore financialStatementStore;

	@MockitoBean
	private SecFilingStore secFilingStore;

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
		when(companySearchService.searchCompanies("apple"))
				.thenReturn(List.of(new CompanySearchResult("Apple Inc.", "0000320193", List.of("AAPL"))));

		mockMvc.perform(get("/companies/search").param("q", "apple").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Apple Inc."))
				.andExpect(jsonPath("$[0].cik").value("0000320193"));
	}

	@Test
	void returns200WithEmptyListWhenNoResults() throws Exception {
		when(companySearchService.searchCompanies("unknownxyz")).thenReturn(List.of());

		mockMvc.perform(
				get("/companies/search").param("q", "unknownxyz").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void stripsWhitespaceFromQueryBeforeDelegating() throws Exception {
		when(companySearchService.searchCompanies("apple")).thenReturn(List.of());

		mockMvc.perform(
				get("/companies/search").param("q", "  apple  ").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk());
	}
}

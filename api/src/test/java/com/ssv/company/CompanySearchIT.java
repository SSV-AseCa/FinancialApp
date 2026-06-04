package com.ssv.company;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.application.CompanySearchService;
import com.ssv.company.dto.CompanySearchResult;

@Import({TestcontainersConfiguration.class, CompanySearchIT.MockConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class CompanySearchIT {

	@TestConfiguration
	static class MockConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}

		@Bean
		@Primary
		public CompanySearchService companySearchService() {
			return new CompanySearchService(null, null, null) {

				@Override
				public List<CompanySearchResult> searchCompanies(String query) {
					if ("apple".equals(query)) {
						return List.of(new CompanySearchResult("Apple Inc.", "0000320193", List.of("AAPL")),
								new CompanySearchResult("Apple Hospitality REIT", "0001418121", List.of()));
					}
					return List.of();
				}
			};
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(get("/companies/search").param("q", "apple")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenQueryIsMissing() throws Exception {
		mockMvc.perform(get("/companies/search").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns400WhenQueryIsBlank() throws Exception {
		mockMvc.perform(get("/companies/search").param("q", "  ").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returnsMatchingCompanies() throws Exception {
		mockMvc.perform(get("/companies/search").param("q", "apple").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Apple Inc."))
				.andExpect(jsonPath("$[0].cik").value("0000320193"))
				.andExpect(jsonPath("$[1].name").value("Apple Hospitality REIT"));
	}

	@Test
	void returnsEmptyListForUnknownQuery() throws Exception {
		mockMvc.perform(
				get("/companies/search").param("q", "unknownxyz").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
	}
}

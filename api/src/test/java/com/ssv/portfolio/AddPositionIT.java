package com.ssv.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.StubPriceProviderConfig;

@Import({TestcontainersConfiguration.class, AddPositionIT.MockJwtConfig.class, StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class AddPositionIT {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	// Companies are identified by CIK; the buy/add flow resolves them to a symbol.
	private static final String AAPL_CIK = "0000320193";
	private static final String TSLA_CIK = "0001318605";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private InvestorProvisioningService provisioningService;
	@Autowired
	private CompanyStore companyRepository;

	@BeforeEach
	void seedCompanies() {
		seedCompany(AAPL_CIK, "AAPL", "Apple Inc.");
		seedCompany(TSLA_CIK, "TSLA", "Tesla Inc.");
	}

	private void seedCompany(String cik, String symbol, String name) {
		if (companyRepository.findByCik(cik).isEmpty()) {
			companyRepository.save(new Company(cik, symbol, name));
		}
	}

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(post("/portfolio/positions").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"" + AAPL_CIK + "\",\"quantity\":10,\"operationDate\":\"2024-01-15\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void resolvesCikToSymbolAndPositionAppearsInPortfolio() throws Exception {
		String sub = "auth0|add-position-it-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);

		mockMvc.perform(authenticatedPost(sub, TSLA_CIK, 5, "2024-06-01")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.ticker").value("TSLA"))
				.andExpect(jsonPath("$.quantity").value(5)).andExpect(jsonPath("$.operationDate").value("2024-06-01"));

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions[0].ticker").value("TSLA"));
	}

	@Test
	void rejectsCikThatNoCompanyMatches() throws Exception {
		String sub = "auth0|add-position-it-unknown-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		// A non-numeric CIK matches no company and is rejected before any EDGAR call.
		mockMvc.perform(authenticatedPost(sub, "not-a-cik", 5, "2024-01-15")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void positionIsOnlyScopedToAuthenticatedInvestor() throws Exception {
		String subA = "auth0|add-position-it-a-" + UUID.randomUUID();
		String subB = "auth0|add-position-it-b-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(subA);
		provisioningService.provisionIfAbsent(subB);

		mockMvc.perform(authenticatedPost(subA, AAPL_CIK, 3, "2024-01-10")).andExpect(status().isCreated());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subB))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions").isEmpty());
	}

	@Test
	void returns400WhenCikIsBlank() throws Exception {
		String sub = "auth0|add-position-it-val-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(authenticatedPost(sub, "  ", 10, "2024-01-15")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsNotPositive() throws Exception {
		String sub = "auth0|add-position-it-qty-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(authenticatedPost(sub, AAPL_CIK, 0, "2024-01-15")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenDateIsInvalid() throws Exception {
		String sub = "auth0|add-position-it-date-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		String body = "{\"cik\":\"" + AAPL_CIK + "\",\"quantity\":5,\"operationDate\":\"not-a-date\"}";
		mockMvc.perform(
				post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))
						.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	private MockHttpServletRequestBuilder authenticatedPost(String sub, String cik, int qty, String date) {
		String body = "{\"cik\":\"%s\",\"quantity\":%d,\"operationDate\":\"%s\"}".formatted(cik, qty, date);
		return post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))
				.contentType(MediaType.APPLICATION_JSON).content(body);
	}
}

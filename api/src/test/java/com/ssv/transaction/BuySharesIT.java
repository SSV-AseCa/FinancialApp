package com.ssv.transaction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.domain.Company;
import com.ssv.company.application.CompanyStore;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.StubPriceProviderConfig;

@Import({TestcontainersConfiguration.class, BuySharesIT.MockJwtConfig.class, StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class BuySharesIT {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private InvestorProvisioningService provisioningService;
	@Autowired
	private CompanyStore companyRepository;

	@BeforeEach
	void seedCompanies() {
		seedCompany("0000320193", "AAPL", "Apple Inc.");
		seedCompany("0000789019", "MSFT", "Microsoft Corp.");
	}

	private void seedCompany(String cik, String symbol, String name) {
		if (companyRepository.findByCik(cik).isEmpty()) {
			companyRepository.save(new Company(cik, symbol, name));
		}
	}

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenCikIsBlank() throws Exception {
		String sub = "auth0|buy-it-blank-cik-";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"\",\"quantity\":10}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns400WhenQuantityIsNotPositive() throws Exception {
		String sub = "auth0|buy-it-bad-qty-";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":0}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createsTransactionAndPositionOnFirstBuy() throws Exception {
		String sub = "auth0|buy-it-first-buy-";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.cik").value("0000320193"))
				.andExpect(jsonPath("$.quantity").value(10)).andExpect(jsonPath("$.type").value("BUY"));

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions[0].ticker").value("AAPL"))
				.andExpect(jsonPath("$.positions[0].quantity").value(10));
	}

	@Test
	void accumulatesQuantityOnSubsequentBuyOfSameCik() throws Exception {
		String sub = "auth0|buy-it-accumulate-";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000789019\",\"quantity\":5}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000789019\",\"quantity\":3}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions[0].quantity").value(8));
	}

	@Test
	void buyDoesNotAffectAnotherInvestorsPortfolio() throws Exception {
		String subA = "auth0|buy-it-scope-a-";
		String subB = "auth0|buy-it-scope-b-";
		provisioningService.provisionIfAbsent(subA);
		provisioningService.provisionIfAbsent(subB);

		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subA))))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subB))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions").isEmpty());
	}
}

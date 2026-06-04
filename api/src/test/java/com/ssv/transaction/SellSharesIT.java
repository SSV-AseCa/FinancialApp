package com.ssv.transaction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ssv.investor.application.InvestorProvisioningService;

@Import({TestcontainersConfiguration.class, SellSharesIT.MockJwtConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class SellSharesIT {

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

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenCikIsBlank() throws Exception {
		String sub = "auth0|sell-it-blank-cik";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"\",\"quantity\":5}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns422WhenNoPositionExists() throws Exception {
		String sub = "auth0|sell-it-no-position";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns422WhenInsufficientShares() throws Exception {
		String sub = "auth0|sell-it-insufficient";
		provisioningService.provisionIfAbsent(sub);
		buy(sub, "0000320193", 3);

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.message").value("Insufficient shares: holds 3 but requested 5"));
	}

	@Test
	void sellsPartiallyAndReducesPositionQuantity() throws Exception {
		String sub = "auth0|sell-it-partial";
		provisioningService.provisionIfAbsent(sub);
		buy(sub, "0000320193", 10);

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":4}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.type").value("SELL"))
				.andExpect(jsonPath("$.quantity").value(4));

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions[0].quantity").value(6));
	}

	@Test
	void sellsAllAndRemovesPosition() throws Exception {
		String sub = "auth0|sell-it-all";
		provisioningService.provisionIfAbsent(sub);
		buy(sub, "0000320193", 5);

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions").isEmpty());
	}

	@Test
	void sellDoesNotAffectAnotherInvestorsPortfolio() throws Exception {
		String subA = "auth0|sell-it-scope-a";
		String subB = "auth0|sell-it-scope-b";
		provisioningService.provisionIfAbsent(subA);
		provisioningService.provisionIfAbsent(subB);
		buy(subA, "0000320193", 10);
		buy(subB, "0000320193", 5);

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subA))))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subB))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions[0].quantity").value(5));
	}

	private void buy(String sub, String cik, int quantity) throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"" + cik + "\",\"quantity\":" + quantity + "}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated());
	}
}

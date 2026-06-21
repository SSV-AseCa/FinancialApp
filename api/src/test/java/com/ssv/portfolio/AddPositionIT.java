package com.ssv.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private InvestorProvisioningService provisioningService;

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(post("/portfolio/positions").contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\"AAPL\",\"quantity\":10,\"operationDate\":\"2024-01-15\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns201AndPositionAppearsInPortfolio() throws Exception {
		String sub = "auth0|add-position-it-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);

		mockMvc.perform(authenticatedPost(sub, "TSLA", 5, "2024-06-01")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.ticker").value("TSLA"))
				.andExpect(jsonPath("$.quantity").value(5)).andExpect(jsonPath("$.operationDate").value("2024-06-01"));

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions[0].ticker").value("TSLA"));
	}

	@Test
	void positionIsOnlyScopedToAuthenticatedInvestor() throws Exception {
		String subA = "auth0|add-position-it-a-" + UUID.randomUUID();
		String subB = "auth0|add-position-it-b-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(subA);
		provisioningService.provisionIfAbsent(subB);

		mockMvc.perform(authenticatedPost(subA, "AAPL", 3, "2024-01-10")).andExpect(status().isCreated());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subB))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions").isEmpty());
	}

	@Test
	void returns400WhenTickerIsBlank() throws Exception {
		String sub = "auth0|add-position-it-val-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(authenticatedPost(sub, "  ", 10, "2024-01-15")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsNotPositive() throws Exception {
		String sub = "auth0|add-position-it-qty-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(authenticatedPost(sub, "AAPL", 0, "2024-01-15")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenDateIsInvalid() throws Exception {
		String sub = "auth0|add-position-it-date-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);
		String body = "{\"ticker\":\"AAPL\",\"quantity\":5,\"operationDate\":\"not-a-date\"}";
		mockMvc.perform(
				post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))
						.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	private MockHttpServletRequestBuilder authenticatedPost(String sub, String ticker, int qty, String date) {
		String body = "{\"ticker\":\"%s\",\"quantity\":%d,\"operationDate\":\"%s\"}".formatted(ticker, qty, date);
		return post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))
				.contentType(MediaType.APPLICATION_JSON).content(body);
	}
}

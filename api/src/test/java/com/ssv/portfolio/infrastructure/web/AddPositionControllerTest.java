package com.ssv.portfolio.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.fake.FakePortfolioService;

@WebMvcTest(PortfolioController.class)
@Import(AddPositionControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class AddPositionControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakePortfolioService portfolioService() {
			return new FakePortfolioService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakePortfolioService portfolioService;

	@BeforeEach
	void reset() {
		portfolioService.reset();
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"AAPL\",\"quantity\":10,\"operationDate\":\"2024-01-15\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns201WithCreatedPositionOnSuccess() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();
		PositionResponse created = new PositionResponse(positionId, "AAPL", 10, LocalDate.of(2024, 1, 15));
		portfolioService.respondWithPosition(created);

		mockMvc.perform(authenticatedPost(investorId, "AAPL", 10, "2024-01-15")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(positionId.toString())).andExpect(jsonPath("$.ticker").value("AAPL"))
				.andExpect(jsonPath("$.quantity").value(10)).andExpect(jsonPath("$.operationDate").value("2024-01-15"));
	}

	@Test
	void returns400WhenTickerIsBlank() throws Exception {
		mockMvc.perform(authenticatedPost(UUID.randomUUID(), "  ", 10, "2024-01-15")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsZero() throws Exception {
		mockMvc.perform(authenticatedPost(UUID.randomUUID(), "AAPL", 0, "2024-01-15"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsNegative() throws Exception {
		mockMvc.perform(authenticatedPost(UUID.randomUUID(), "AAPL", -5, "2024-01-15"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenOperationDateIsMissing() throws Exception {
		UUID investorId = UUID.randomUUID();
		mockMvc.perform(post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"cik\":\"AAPL\",\"quantity\":10}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenDateFormatIsInvalid() throws Exception {
		UUID investorId = UUID.randomUUID();
		mockMvc.perform(post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"AAPL\",\"quantity\":10,\"operationDate\":\"not-a-date\"}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenBodyIsMissing() throws Exception {
		UUID investorId = UUID.randomUUID();
		mockMvc.perform(post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	private MockHttpServletRequestBuilder authenticatedPost(UUID investorId, String cik, int qty, String date) {
		String body = "{\"cik\":\"%s\",\"quantity\":%d,\"operationDate\":\"%s\"}".formatted(cik, qty, date);
		return post("/portfolio/positions").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON).content(body);
	}
}

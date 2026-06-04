package com.ssv.portfolio.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.fake.FakePortfolioService;

@WebMvcTest(PortfolioController.class)
@Import(PortfolioControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PortfolioControllerTest {

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
		mockMvc.perform(get("/portfolio")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsPortfolioWithPositionsForAuthenticatedUser() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();

		PortfolioResponse response = new PortfolioResponse(portfolioId,
				List.of(new PositionResponse(positionId, "AAPL", 10, LocalDate.of(2024, 1, 15))));
		portfolioService.respondWithPortfolio(response);

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(portfolioId.toString()))
				.andExpect(jsonPath("$.positions[0].id").value(positionId.toString()))
				.andExpect(jsonPath("$.positions[0].ticker").value("AAPL"))
				.andExpect(jsonPath("$.positions[0].quantity").value(10))
				.andExpect(jsonPath("$.positions[0].operationDate").value("2024-01-15"));
	}

	@Test
	void returnsEmptyPositionsListWhenPortfolioIsEmpty() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		portfolioService.respondWithPortfolio(new PortfolioResponse(portfolioId, List.of()));

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(portfolioId.toString())).andExpect(jsonPath("$.positions").isEmpty());
	}

	private MockHttpServletRequestBuilder authenticatedRequest(UUID investorId) {
		return get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId);
	}
}

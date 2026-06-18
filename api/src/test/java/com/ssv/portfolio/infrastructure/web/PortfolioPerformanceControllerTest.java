package com.ssv.portfolio.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import com.ssv.portfolio.fake.FakePortfolioPerformanceService;
import com.ssv.portfolio.dto.PortfolioPerformanceResponse;

@WebMvcTest(PortfolioValueController.class)
@Import(PortfolioPerformanceControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PortfolioPerformanceControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakePortfolioPerformanceService portfolioPerformanceService() {
			return new FakePortfolioPerformanceService();
		}

		@Bean
		com.ssv.portfolio.fake.FakePortfolioValueService portfolioValueService() {
			return new com.ssv.portfolio.fake.FakePortfolioValueService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakePortfolioPerformanceService portfolioPerformanceService;

	@BeforeEach
	void reset() {
		portfolioPerformanceService.reset();
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/portfolio/performance")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsPerformanceWithTotalsForAuthenticatedUser() throws Exception {
		UUID investorId = UUID.randomUUID();
		portfolioPerformanceService
				.respondWith(new PortfolioPerformanceResponse(new BigDecimal("1500.00"), new BigDecimal("500.00")));

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalValue").value(1500.00)).andExpect(jsonPath("$.totalPnL").value(500.00));
	}

	@Test
	void returnsZeroesWhenEmptyPortfolio() throws Exception {
		UUID investorId = UUID.randomUUID();
		portfolioPerformanceService.respondWith(new PortfolioPerformanceResponse(BigDecimal.ZERO, BigDecimal.ZERO));

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalValue").value(0)).andExpect(jsonPath("$.totalPnL").value(0));
	}

	private MockHttpServletRequestBuilder authenticatedRequest(UUID investorId) {
		return get("/portfolio/performance").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId);
	}
}

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
import com.ssv.portfolio.dto.PortfolioValueResponse;
import com.ssv.portfolio.fake.FakePortfolioValueService;

@WebMvcTest(PortfolioValueController.class)
@Import(PortfolioValueControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PortfolioValueControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakePortfolioValueService portfolioValueService() {
			return new FakePortfolioValueService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakePortfolioValueService portfolioValueService;

	@BeforeEach
	void reset() {
		portfolioValueService.reset();
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/portfolio/value")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsTotalValueForAuthenticatedUser() throws Exception {
		UUID investorId = UUID.randomUUID();
		portfolioValueService.respondWith(new PortfolioValueResponse(new BigDecimal("1500.00")));

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalValue").value(1500.00));
	}

	@Test
	void returnsTotalValueOfZeroWhenPortfolioIsEmpty() throws Exception {
		UUID investorId = UUID.randomUUID();
		portfolioValueService.respondWith(new PortfolioValueResponse(BigDecimal.ZERO));

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalValue").value(0));
	}

	private MockHttpServletRequestBuilder authenticatedRequest(UUID investorId) {
		return get("/portfolio/value").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId);
	}
}

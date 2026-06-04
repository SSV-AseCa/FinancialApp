package com.ssv.portfolio.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.portfolio.fake.FakePortfolioService;

@WebMvcTest(PortfolioController.class)
@Import(RemovePositionControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class RemovePositionControllerTest {

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
		mockMvc.perform(
				delete("/portfolio/positions/" + UUID.randomUUID()).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns204OnSuccessfulDelete() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();

		mockMvc.perform(delete("/portfolio/positions/" + positionId).with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId))
				.andExpect(status().isNoContent());
	}

	@Test
	void returns404WhenPositionNotFound() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();
		portfolioService.throwOnNextCall(new PositionNotFoundException(positionId));

		mockMvc.perform(delete("/portfolio/positions/" + positionId).with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)).andExpect(status().isNotFound());
	}
}

package com.ssv.portfolio.infrastructure.web;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.application.PortfolioService;
import com.ssv.portfolio.exceptions.PositionNotFoundException;

@WebMvcTest(PortfolioController.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class RemovePositionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PortfolioService portfolioService;

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
		doNothing().when(portfolioService).removePosition(investorId, positionId);

		mockMvc.perform(delete("/portfolio/positions/" + positionId).with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId))
				.andExpect(status().isNoContent());
	}

	@Test
	void returns404WhenPositionNotFound() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();
		doThrow(new PositionNotFoundException(positionId)).when(portfolioService).removePosition(investorId,
				positionId);

		mockMvc.perform(delete("/portfolio/positions/" + positionId).with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)).andExpect(status().isNotFound());
	}
}

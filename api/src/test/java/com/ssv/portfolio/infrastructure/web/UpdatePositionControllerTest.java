package com.ssv.portfolio.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.portfolio.fake.FakePortfolioService;

@WebMvcTest(PortfolioController.class)
@Import(UpdatePositionControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class UpdatePositionControllerTest {

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
		mockMvc.perform(put("/portfolio/positions/" + UUID.randomUUID())
				.with(SecurityMockMvcRequestPostProcessors.csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\"AAPL\",\"quantity\":10,\"operationDate\":\"2024-01-15\",\"costBasis\":120.5}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns200WithUpdatedPositionOnSuccess() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();
		PositionResponse updated = new PositionResponse(positionId, "MSFT", 20, LocalDate.of(2024, 6, 1));
		portfolioService.respondWithPosition(updated);

		mockMvc.perform(authenticatedPut(investorId, positionId, "MSFT", 20, "2024-06-01")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(positionId.toString())).andExpect(jsonPath("$.ticker").value("MSFT"))
				.andExpect(jsonPath("$.quantity").value(20)).andExpect(jsonPath("$.operationDate").value("2024-06-01"));
	}

	@Test
	void returns404WhenPositionNotFound() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();
		portfolioService.throwOnNextCall(new PositionNotFoundException(positionId));

		mockMvc.perform(authenticatedPut(investorId, positionId, "AAPL", 10, "2024-01-15"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenTickerIsBlank() throws Exception {
		mockMvc.perform(authenticatedPut(UUID.randomUUID(), UUID.randomUUID(), "  ", 10, "2024-01-15"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsZero() throws Exception {
		mockMvc.perform(authenticatedPut(UUID.randomUUID(), UUID.randomUUID(), "AAPL", 0, "2024-01-15"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenOperationDateIsMissing() throws Exception {
		UUID investorId = UUID.randomUUID();
		mockMvc.perform(
				put("/portfolio/positions/" + UUID.randomUUID()).with(SecurityMockMvcRequestPostProcessors.jwt())
						.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
						.contentType(MediaType.APPLICATION_JSON).content("{\"ticker\":\"AAPL\",\"quantity\":10}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenDateFormatIsInvalid() throws Exception {
		UUID investorId = UUID.randomUUID();
		mockMvc.perform(
				put("/portfolio/positions/" + UUID.randomUUID()).with(SecurityMockMvcRequestPostProcessors.jwt())
						.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ticker\":\"AAPL\",\"quantity\":10,\"operationDate\":\"not-a-date\"}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	private MockHttpServletRequestBuilder authenticatedPut(UUID investorId, UUID positionId, String ticker, int qty,
			String date) {
		String body = "{\"ticker\":\"%s\",\"quantity\":%d,\"operationDate\":\"%s\",\"costBasis\":%s}".formatted(ticker,
				qty, date, java.math.BigDecimal.valueOf(100));
		return put("/portfolio/positions/" + positionId).with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON).content(body);
	}
}

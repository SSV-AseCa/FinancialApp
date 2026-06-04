package com.ssv.transaction.infrastructure.web;

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

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.exceptions.BusinessRuleException;
import com.ssv.transaction.fake.FakeTransactionHistoryService;
import com.ssv.transaction.fake.FakeTransactionService;

@WebMvcTest(TransactionController.class)
@Import(SellSharesControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class SellSharesControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakeTransactionService transactionService() {
			return new FakeTransactionService();
		}

		@Bean
		FakeTransactionHistoryService transactionHistoryService() {
			return new FakeTransactionHistoryService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeTransactionService transactionService;

	@Autowired
	private FakeTransactionHistoryService transactionHistoryService;

	@BeforeEach
	void reset() {
		transactionService.reset();
		transactionHistoryService.reset();
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenCikIsBlank() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"\",\"quantity\":5}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsZero() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":0}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsNegative() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":-1}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns201WithTransactionOnSuccess() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		UUID txId = UUID.randomUUID();
		TransactionResponse response = new TransactionResponse(txId, portfolioId, "0000320193", 5, TransactionType.SELL,
				LocalDate.of(2024, 6, 1));
		transactionService.respondWith(response);

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.type").value("SELL")).andExpect(jsonPath("$.quantity").value(5))
				.andExpect(jsonPath("$.cik").value("0000320193"));
	}

	@Test
	void returns422WhenInsufficientShares() throws Exception {
		UUID investorId = UUID.randomUUID();
		transactionService.throwOnNextCall(new BusinessRuleException("Insufficient shares: holds 3 but requested 5"));

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":5}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.message").value("Insufficient shares: holds 3 but requested 5"));
	}

	@Test
	void returns422WhenSellingSharesNotOwned() throws Exception {
		UUID investorId = UUID.randomUUID();
		transactionService.throwOnNextCall(new BusinessRuleException("No position found for CIK 9999999999"));

		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"9999999999\",\"quantity\":1}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId))
				.andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.message").exists());
	}
}

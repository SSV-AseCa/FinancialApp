package com.ssv.transaction.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.transaction.application.TransactionHistoryService;
import com.ssv.transaction.application.TransactionService;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.exceptions.BusinessRuleException;

@WebMvcTest(TransactionController.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class BuySharesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransactionService transactionService;

	@MockitoBean
	private TransactionHistoryService transactionHistoryService;

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenCikIsBlank() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"\",\"quantity\":10}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsZero() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":0}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsNegative() throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":-5}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns201WithTransactionOnSuccess() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		UUID txId = UUID.randomUUID();
		TransactionResponse response = new TransactionResponse(txId, portfolioId, "0000320193", 10, TransactionType.BUY,
				LocalDate.of(2024, 1, 15));
		when(transactionService.buy(eq(investorId), any(BuyRequest.class))).thenReturn(response);

		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(txId.toString())).andExpect(jsonPath("$.cik").value("0000320193"))
				.andExpect(jsonPath("$.quantity").value(10)).andExpect(jsonPath("$.type").value("BUY"));
	}

	@Test
	void returns422OnBusinessRuleViolation() throws Exception {
		UUID investorId = UUID.randomUUID();
		when(transactionService.buy(eq(investorId), any()))
				.thenThrow(new BusinessRuleException("Business rule violated"));

		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"0000320193\",\"quantity\":10}").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId))
				.andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.message").exists());
	}
}

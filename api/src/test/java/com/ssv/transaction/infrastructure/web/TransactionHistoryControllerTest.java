package com.ssv.transaction.infrastructure.web;

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

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.fake.FakeTransactionHistoryService;
import com.ssv.transaction.fake.FakeTransactionService;

@WebMvcTest(TransactionController.class)
@Import(TransactionHistoryControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class TransactionHistoryControllerTest {

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
		mockMvc.perform(get("/portfolio/transactions")).andExpect(status().isUnauthorized());
	}

	@Test
	void returns200WithTransactionListForAuthenticatedUser() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		List<TransactionResponse> history = List.of(
				new TransactionResponse(UUID.randomUUID(), portfolioId, "0000320193", 10, TransactionType.BUY,
						LocalDate.of(2024, 6, 1)),
				new TransactionResponse(UUID.randomUUID(), portfolioId, "0000320193", 5, TransactionType.SELL,
						LocalDate.of(2024, 1, 1)));
		transactionHistoryService.respondWith(history);

		mockMvc.perform(get("/portfolio/transactions").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].type").value("BUY")).andExpect(jsonPath("$[0].cik").value("0000320193"))
				.andExpect(jsonPath("$[0].quantity").value(10))
				.andExpect(jsonPath("$[0].transactionDate").value("2024-06-01"))
				.andExpect(jsonPath("$[1].type").value("SELL"));
	}

	@Test
	void returns200WithEmptyListWhenNoTransactions() throws Exception {
		UUID investorId = UUID.randomUUID();

		mockMvc.perform(get("/portfolio/transactions").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
	}
}

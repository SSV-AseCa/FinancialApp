package com.ssv.transaction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.domain.Company;
import com.ssv.company.application.CompanyStore;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.StubPriceProviderConfig;

@Import({TestcontainersConfiguration.class, TransactionHistoryIT.MockJwtConfig.class, StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class TransactionHistoryIT {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private InvestorProvisioningService provisioningService;
	@Autowired
	private CompanyStore companyRepository;

	@BeforeEach
	void seedCompanies() {
		seedCompany("0000320193", "AAPL", "Apple Inc.");
		seedCompany("0000789019", "MSFT", "Microsoft Corp.");
	}

	private void seedCompany(String cik, String symbol, String name) {
		if (companyRepository.findByCik(cik).isEmpty()) {
			companyRepository.save(new Company(cik, symbol, name));
		}
	}

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(get("/portfolio/transactions")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsEmptyListForNewInvestor() throws Exception {
		String sub = "auth0|tx-history-empty";
		provisioningService.provisionIfAbsent(sub);
		mockMvc.perform(get("/portfolio/transactions")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))).andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void returnsTransactionsAfterBuyAndSell() throws Exception {
		String sub = "auth0|tx-history-buy-sell";
		provisioningService.provisionIfAbsent(sub);
		buy(sub, "0000320193", 10);
		buy(sub, "0000789019", 5);
		sell(sub, "0000320193", 3);

		mockMvc.perform(get("/portfolio/transactions")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].type").value("SELL"))
				.andExpect(jsonPath("$[0].cik").value("0000320193")).andExpect(jsonPath("$[0].quantity").value(3));
	}

	@Test
	void returnsNewestTransactionFirst() throws Exception {
		String sub = "auth0|tx-history-order";
		provisioningService.provisionIfAbsent(sub);
		buy(sub, "0000320193", 5);
		buy(sub, "0000789019", 3);

		mockMvc.perform(get("/portfolio/transactions")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].type").value("BUY"))
				.andExpect(jsonPath("$[1].type").value("BUY"));
	}

	@Test
	void investorOnlySeesOwnTransactions() throws Exception {
		String subA = "auth0|tx-history-scope-a";
		String subB = "auth0|tx-history-scope-b";
		provisioningService.provisionIfAbsent(subA);
		provisioningService.provisionIfAbsent(subB);
		buy(subA, "0000320193", 10);
		buy(subB, "0000789019", 5);

		mockMvc.perform(get("/portfolio/transactions")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subA)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].cik").value("0000320193"));
	}

	private void buy(String sub, String cik, int qty) throws Exception {
		mockMvc.perform(post("/portfolio/transactions/buy").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"" + cik + "\",\"quantity\":" + qty + "}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated());
	}

	private void sell(String sub, String cik, int qty) throws Exception {
		mockMvc.perform(post("/portfolio/transactions/sell").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"" + cik + "\",\"quantity\":" + qty + "}")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isCreated());
	}
}

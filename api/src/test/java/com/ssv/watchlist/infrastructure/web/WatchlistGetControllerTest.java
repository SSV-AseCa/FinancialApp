package com.ssv.watchlist.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.ssv.watchlist.fake.FakeWatchlistQueryService;
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
import com.ssv.watchlist.dto.CurrentFinancialMetrics;
import com.ssv.watchlist.dto.WatchlistCompanyResponse;

@WebMvcTest(WatchlistController.class)
@Import(WatchlistGetControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class WatchlistGetControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakeWatchlistQueryService watchlistService() {
			return new FakeWatchlistQueryService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeWatchlistQueryService watchlistService;

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/watchlist").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void emptyWatchlistReturnsEmptyArray() throws Exception {
		UUID investorId = UUID.randomUUID();
		mockMvc.perform(get("/watchlist").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void returnsMultipleEntriesWithMetrics() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		WatchlistCompanyResponse item = new WatchlistCompanyResponse(companyId, "0000320193", "AAPL", "Apple Inc",
				new CurrentFinancialMetrics(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
						new BigDecimal("4")));
		watchlistService.respondWithList(List.of(item));

		mockMvc.perform(get("/watchlist").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].companyId").value(companyId.toString()))
				.andExpect(jsonPath("$[0].cik").value("0000320193"))
				.andExpect(jsonPath("$[0].metrics.revenue").value(1));
	}
}

package com.ssv.watchlist.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.watchlist.application.WatchlistCompareService;
import com.ssv.watchlist.dto.CurrentFinancialMetrics;
import com.ssv.watchlist.dto.WatchlistCompareCompanyResponse;
import com.ssv.watchlist.dto.WatchlistCompareResponse;

@WebMvcTest(WatchlistCompareController.class)
@Import(WatchlistCompareControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class WatchlistCompareControllerTest {

	@TestConfiguration
	static class Config {
		@Bean
		WatchlistCompareService watchlistCompareService() {
			return Mockito.mock(WatchlistCompareService.class);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WatchlistCompareService watchlistCompareService;

	@BeforeEach
	void reset() {
		Mockito.reset(watchlistCompareService);
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/watchlist/compare").param("ciks", "0000320193,0000789019"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns400WhenCiksParamIsMissing() throws Exception {
		mockMvc.perform(get("/watchlist/compare").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns400WhenFewerThanTwoCiksProvided() throws Exception {
		Mockito.when(watchlistCompareService.compare(Mockito.any(), Mockito.eq("0000320193")))
				.thenThrow(new IllegalArgumentException("At least two distinct CIKs must be provided"));
		mockMvc.perform(
				get("/watchlist/compare").param("ciks", "0000320193").with(SecurityMockMvcRequestPostProcessors.jwt()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns200WhenAllRequestedWatched() throws Exception {
		var c1 = new WatchlistCompareCompanyResponse(UUID.randomUUID(), "0000320193", "AAPL", "Apple",
				new CurrentFinancialMetrics(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
						new BigDecimal("4")));
		var c2 = new WatchlistCompareCompanyResponse(UUID.randomUUID(), "0000789019", "MSFT", "Microsoft",
				new CurrentFinancialMetrics(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"),
						new BigDecimal("40")));
		var resp = new WatchlistCompareResponse(List.of(c1, c2));
		org.mockito.Mockito.when(watchlistCompareService.compare(org.mockito.Mockito.any(),
				org.mockito.Mockito.eq("0000320193,0000789019"))).thenReturn(resp);

		mockMvc.perform(get("/watchlist/compare").param("ciks", "0000320193,0000789019")
				.with(SecurityMockMvcRequestPostProcessors.jwt())).andExpect(status().isOk())
				.andExpect(jsonPath("$.companies[0].cik").value("0000320193"))
				.andExpect(jsonPath("$.companies[1].cik").value("0000789019"))
				.andExpect(jsonPath("$.companies[0].metrics.revenue").value(1));
	}
}

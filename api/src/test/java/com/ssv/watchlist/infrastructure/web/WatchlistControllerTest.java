package com.ssv.watchlist.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ssv.watchlist.dto.WatchlistResponse;
import com.ssv.watchlist.fake.FakeWatchlistService;

@WebMvcTest(WatchlistController.class)
@Import(WatchlistControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class WatchlistControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		FakeWatchlistService watchlistService() {
			return new FakeWatchlistService();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakeWatchlistService watchlistService;

	@BeforeEach
	void reset() {
		watchlistService.reset();
	}

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(post("/watchlist").with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"cik\":\"0000320193\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns201WithCreatedOnSuccess() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID entryId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		WatchlistResponse created = new WatchlistResponse(entryId, companyId, "0000320193");
		watchlistService.respondWith(created);

		mockMvc.perform(authenticatedPost(investorId, "0000320193")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(entryId.toString()))
				.andExpect(jsonPath("$.companyId").value(companyId.toString()))
				.andExpect(jsonPath("$.cik").value("0000320193"));
	}

	@Test
	void returns400WhenCikIsInvalid() throws Exception {
		mockMvc.perform(post("/watchlist").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON).content("{\"cik\":\"not-a-number\"}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns409WhenDuplicateCompany() throws Exception {
		UUID investorId = UUID.randomUUID();
		watchlistService.respondWithError(new com.ssv.watchlist.exceptions.DuplicateWatchlistEntryException("dup"));

		mockMvc.perform(authenticatedPost(investorId, "0000320193")).andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").exists());
	}

	private MockHttpServletRequestBuilder authenticatedPost(UUID investorId, String cik) {
		String body = "{\"cik\":\"%s\"}".formatted(cik);
		return post("/watchlist").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId)
				.contentType(MediaType.APPLICATION_JSON).content(body);
	}
}

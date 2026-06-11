package com.ssv.watchlist;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.domain.Company;
import com.ssv.company.application.CompanyStore;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.watchlist.domain.WatchlistEntry;
import com.ssv.watchlist.infrastructure.persistence.WatchlistRepository;

@Import({TestcontainersConfiguration.class, WatchlistIT.MockJwtConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class WatchlistIT {

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
	private CompanyStore companyStore;

	@Autowired
	private WatchlistRepository watchlistRepository;

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(delete("/watchlist/0000320193").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns404WhenCompanyDoesNotExist() throws Exception {
		String sub = "auth0|watchlist-it-user-1-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);

		mockMvc.perform(delete("/watchlist/0000999999")
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isNotFound());
	}

	@Test
	void returns404WhenCompanyIsNotOnWatchlist() throws Exception {
		String sub = "auth0|watchlist-it-user-2-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);

		String cik = "0000320193";
		if (companyStore.findByCik(cik).isEmpty()) {
			companyStore.save(new Company(cik, "AAPL", "Apple Inc"));
		}

		mockMvc.perform(
				delete("/watchlist/" + cik).with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isNotFound());
	}

	@Test
	void deletesWatchlistEntrySuccessfully() throws Exception {
		String sub = "auth0|watchlist-it-user-3-" + UUID.randomUUID();
		UUID investorId = provisioningService.provisionIfAbsent(sub);

		String cik = "0000320193";
		Company company = companyStore.findByCik(cik)
				.orElseGet(() -> companyStore.save(new Company(cik, "AAPL", "Apple Inc")));

		WatchlistEntry entry = new WatchlistEntry();
		entry.setInvestorId(investorId);
		entry.setCompanyId(company.getId());
		watchlistRepository.save(entry);

		assertTrue(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, company.getId()));

		mockMvc.perform(
				delete("/watchlist/" + cik).with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isNoContent());

		assertFalse(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, company.getId()));
	}

	@Test
	void investorCannotDeleteAnotherInvestorsWatchlistEntry() throws Exception {
		String subOwner = "auth0|watchlist-owner-" + UUID.randomUUID();
		UUID ownerId = provisioningService.provisionIfAbsent(subOwner);

		String subOther = "auth0|watchlist-other-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(subOther);

		String cik = "0000320193";
		Company company = companyStore.findByCik(cik)
				.orElseGet(() -> companyStore.save(new Company(cik, "AAPL", "Apple Inc")));

		WatchlistEntry entry = new WatchlistEntry();
		entry.setInvestorId(ownerId);
		entry.setCompanyId(company.getId());
		watchlistRepository.save(entry);

		mockMvc.perform(delete("/watchlist/" + cik)
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subOther))))
				.andExpect(status().isNotFound());

		assertTrue(watchlistRepository.existsByInvestorIdAndCompanyId(ownerId, company.getId()));
	}

	@Test
	void existingWatchlistEntriesRemainUntouchedAfterDeletingDifferentOne() throws Exception {
		String sub = "auth0|watchlist-it-user-4-" + UUID.randomUUID();
		UUID investorId = provisioningService.provisionIfAbsent(sub);

		String cikApple = "0000320193";
		Company companyApple = companyStore.findByCik(cikApple)
				.orElseGet(() -> companyStore.save(new Company(cikApple, "AAPL", "Apple Inc")));

		String cikMsft = "0000078901";
		Company companyMsft = companyStore.findByCik(cikMsft)
				.orElseGet(() -> companyStore.save(new Company(cikMsft, "MSFT", "Microsoft Corp")));

		WatchlistEntry entryApple = new WatchlistEntry();
		entryApple.setInvestorId(investorId);
		entryApple.setCompanyId(companyApple.getId());
		watchlistRepository.save(entryApple);

		WatchlistEntry entryMsft = new WatchlistEntry();
		entryMsft.setInvestorId(investorId);
		entryMsft.setCompanyId(companyMsft.getId());
		watchlistRepository.save(entryMsft);

		mockMvc.perform(delete("/watchlist/" + cikApple)
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isNoContent());

		assertFalse(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, companyApple.getId()));
		assertTrue(watchlistRepository.existsByInvestorIdAndCompanyId(investorId, companyMsft.getId()));
	}

}

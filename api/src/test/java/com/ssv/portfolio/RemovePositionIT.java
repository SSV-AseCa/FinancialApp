package com.ssv.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.StubPriceProviderConfig;

@Import({TestcontainersConfiguration.class, RemovePositionIT.MockJwtConfig.class, StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class RemovePositionIT {

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
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PositionRepository positionRepository;

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(
				delete("/portfolio/positions/" + UUID.randomUUID()).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns204AndPositionIsRemovedFromPortfolio() throws Exception {
		String sub = "auth0|remove-position-it-" + UUID.randomUUID();
		Position position = createPosition(sub, "AAPL", 5, LocalDate.of(2024, 1, 1));

		mockMvc.perform(delete("/portfolio/positions/" + position.getId())
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.positions").isEmpty());
	}

	@Test
	void returns404WhenPositionBelongsToAnotherInvestor() throws Exception {
		String subOwner = "auth0|remove-position-owner-" + UUID.randomUUID();
		String subOther = "auth0|remove-position-other-" + UUID.randomUUID();
		Position position = createPosition(subOwner, "AAPL", 5, LocalDate.of(2024, 1, 1));
		provisioningService.provisionIfAbsent(subOther);

		mockMvc.perform(delete("/portfolio/positions/" + position.getId())
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(subOther))))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns404WhenPositionDoesNotExist() throws Exception {
		String sub = "auth0|remove-position-noexist-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);

		mockMvc.perform(delete("/portfolio/positions/" + UUID.randomUUID())
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub))))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
	}

	private Position createPosition(String sub, String ticker, int qty, LocalDate date) {
		UUID investorId = provisioningService.provisionIfAbsent(sub);
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId).orElseThrow();
		Position p = new Position();
		p.setPortfolioId(portfolio.getId());
		p.setTicker(ticker);
		p.setQuantity(qty);
		p.setOperationDate(date);
		return positionRepository.save(p);
	}
}

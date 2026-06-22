package com.ssv.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.TestcontainersConfiguration;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.market.fake.StubPriceProviderConfig;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

@Import({TestcontainersConfiguration.class, PortfolioValueIT.MockJwtConfig.class, StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PortfolioValueIT {

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
	@Autowired
	private FakeCurrentPriceProvider priceProvider;

	@BeforeEach
	void resetPrices() {
		// Drop the stub's default price so unstubbed symbols read as having no price.
		priceProvider.reset();
	}

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(get("/portfolio/value")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsZeroWhenPortfolioHasNoPositions() throws Exception {
		String sub = "auth0|portfolio-value-it-empty-" + UUID.randomUUID();
		provisionAndGetPortfolio(sub);

		mockMvc.perform(jwtRequest(sub)).andExpect(status().isOk()).andExpect(jsonPath("$.totalValue").value(0));
	}

	@Test
	void returnsTotalValueCalculatedFromStoredPrices() throws Exception {
		String sub = "auth0|portfolio-value-it-total-" + UUID.randomUUID();
		Portfolio portfolio = provisionAndGetPortfolio(sub);

		positionRepository.save(buildPosition(portfolio.getId(), "AAPL", 10));
		positionRepository.save(buildPosition(portfolio.getId(), "MSFT", 5));
		priceProvider.stub("AAPL", new BigDecimal("150.00"));
		priceProvider.stub("MSFT", new BigDecimal("300.00"));

		// AAPL: 10 × 150 = 1500, MSFT: 5 × 300 = 1500, total = 3000
		mockMvc.perform(jwtRequest(sub)).andExpect(status().isOk()).andExpect(jsonPath("$.totalValue").value(3000.00));
	}

	@Test
	void failsWhenAPositionHasNoPrice() throws Exception {
		String sub = "auth0|portfolio-value-it-no-price-" + UUID.randomUUID();
		Portfolio portfolio = provisionAndGetPortfolio(sub);

		positionRepository.save(buildPosition(portfolio.getId(), "AAPL", 10));
		positionRepository.save(buildPosition(portfolio.getId(), "UNKNOWN", 99));
		priceProvider.stub("AAPL", new BigDecimal("200.00"));

		// an unpriced position fails the whole read rather than being valued at zero
		mockMvc.perform(jwtRequest(sub)).andExpect(status().isServiceUnavailable());
	}

	private Portfolio provisionAndGetPortfolio(String sub) {
		UUID investorId = provisioningService.provisionIfAbsent(sub);
		return portfolioRepository.findByInvestorId(investorId).orElseThrow();
	}

	private static Position buildPosition(UUID portfolioId, String ticker, int quantity) {
		Position p = new Position();
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(quantity);
		p.setOperationDate(LocalDate.of(2024, 1, 1));
		return p;
	}

	private MockHttpServletRequestBuilder jwtRequest(String sub) {
		return get("/portfolio/value").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)));
	}
}

package com.ssv.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.domain.Company;
import com.ssv.company.application.CompanyStore;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.market.fake.StubPriceProviderConfig;
import com.ssv.portfolio.application.PortfolioValueService;
import com.ssv.transaction.application.TransactionService;
import com.ssv.transaction.dto.BuyRequest;

/**
 * Regression guard for a model inconsistency that was fixed: a bought position
 * is keyed by its ticker symbol, so it is matched to a market price and valued
 * instead of always valuing to zero.
 */
@Import({TestcontainersConfiguration.class, PositionModelInconsistencyIT.MockJwtConfig.class,
		StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PositionModelInconsistencyIT {

	// Apple's CIK — the only company identifier the buy endpoint accepts.
	private static final String APPLE_CIK = "0000320193";

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
	private InvestorProvisioningService provisioningService;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private PortfolioValueService portfolioValueService;
	@Autowired
	private FakeCurrentPriceProvider priceProvider;
	@Autowired
	private CompanyStore companyRepository;

	/**
	 * The buy path resolves the CIK to a ticker symbol and stores that on the
	 * position, while valuation prices by symbol. A bought position is therefore
	 * matched to its price rather than always valuing to zero.
	 */
	@Test
	void boughtPositionIsValuedAgainstCurrentPrice() {
		UUID investorId = provisioningService.provisionIfAbsent("auth0|pos-model-it-value-" + UUID.randomUUID());
		// Realistic precondition: the company is known (you viewed/searched it before
		// buying).
		if (companyRepository.findByCik(APPLE_CIK).isEmpty()) {
			companyRepository.save(new Company(APPLE_CIK, "AAPL", "Apple Inc."));
		}

		// Yahoo prices the position by ticker symbol, as it returns them.
		priceProvider.stub("AAPL", new BigDecimal("150.00"));

		transactionService.buy(investorId, new BuyRequest(APPLE_CIK, 10));

		BigDecimal total = portfolioValueService.getPortfolioValue(investorId).totalValue();
		assertEquals(0, new BigDecimal("1500.00").compareTo(total),
				"10 shares × 150.00 should value at 1500.00, but the CIK/ticker mismatch yields " + total);
	}
}

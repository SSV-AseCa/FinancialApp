package com.ssv.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import com.ssv.portfolio.application.PortfolioPositionQueryService;
import com.ssv.portfolio.application.PortfolioService;
import com.ssv.portfolio.application.PortfolioValueService;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.transaction.application.TransactionService;
import com.ssv.transaction.dto.BuyRequest;

/**
 * Regression guard for two model inconsistencies that were fixed: the price-
 * update batch must see the symbols actually held, and a bought position must
 * be valued against stored prices. Both started red and now pass.
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
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioValueService portfolioValueService;
	@Autowired
	private PortfolioPositionQueryService positionQueryService;
	@Autowired
	private FakeCurrentPriceProvider priceProvider;
	@Autowired
	private CompanyStore companyRepository;

	/**
	 * Finding #1: the Yahoo price-update batch sources its ticker list from
	 * {@code PortfolioPositionQueryService.findDistinctSymbols()} (table
	 * {@code portfolio_positions}), but real holdings are written to the
	 * {@code position} table. Nothing writes {@code portfolio_positions}, so the
	 * batch never sees the symbols the investor actually holds.
	 */
	@Test
	void priceBatchSeesSymbolsTheInvestorActuallyHolds() {
		UUID investorId = provisioningService.provisionIfAbsent("auth0|pos-model-it-batch-" + UUID.randomUUID());

		portfolioService.addPosition(investorId, new AddPositionRequest("AAPL", 10, LocalDate.of(2024, 1, 1)));

		List<String> batchSymbols = positionQueryService.findDistinctSymbols();
		assertTrue(batchSymbols.contains("AAPL"),
				"price-update batch should fetch AAPL (a held symbol) but findDistinctSymbols returned "
						+ batchSymbols);
	}

	/**
	 * Finding #2: the buy path stores the CIK in {@code Position.ticker}
	 * ({@code p.setTicker(cik)}), while valuation looks up stored prices by symbol
	 * ({@code MarketPrice.symbol}). A bought position can therefore never be
	 * matched to a price and always values to zero.
	 */
	@Test
	void boughtPositionIsValuedAgainstStoredPrices() {
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

package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class PortfolioPerformanceServiceTest {

	private FakePortfolioRepository portfolioRepo;
	private FakePositionRepository positionRepo;
	private FakeCurrentPriceProvider priceProvider;
	private PortfolioPerformanceService service;

	@BeforeEach
	void setUp() {
		portfolioRepo = new FakePortfolioRepository();
		positionRepo = new FakePositionRepository();
		priceProvider = new FakeCurrentPriceProvider();
		service = new PortfolioPerformanceService(portfolioRepo, positionRepo, priceProvider);
	}

	@Test
	void returnsZeroWhenPortfolioHasNoPositions() {
		UUID investorId = UUID.randomUUID();
		portfolioRepo.seed(portfolio(investorId));

		assertEquals(BigDecimal.ZERO, service.getPortfolioPerformance(investorId).totalValue());
		assertEquals(BigDecimal.ZERO, service.getPortfolioPerformance(investorId).totalPnL());
	}

	@Test
	void calculatesPnLAsCurrentValueMinusRecordedCostBasis() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		portfolioRepo.seed(portfolio);
		// 10 shares bought for a recorded cost basis of 1000 (100 each)
		positionRepo.seed(position(portfolio.getId(), "AAPL", 10, new BigDecimal("1000.00")));
		// latest price: 150.00
		priceProvider.stub("AAPL", new BigDecimal("150.00"));

		// totalValue = 10 * 150 = 1500, totalCost = 1000, totalPnL = 500
		assertEquals(new BigDecimal("1500.00"), service.getPortfolioPerformance(investorId).totalValue());
		assertEquals(new BigDecimal("500.00"), service.getPortfolioPerformance(investorId).totalPnL());
	}

	@Test
	void throwsWhenNoPortfolioFound() {
		assertThrows(IllegalStateException.class, () -> service.getPortfolioPerformance(UUID.randomUUID()));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId, String ticker, int quantity, BigDecimal costBasis) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(quantity);
		p.setCostBasis(costBasis);
		p.setOperationDate(LocalDate.of(2024, 1, 1));
		return p;
	}
}

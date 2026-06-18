package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.market.fake.FakeMarketPriceRepository;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class PortfolioValueServiceTest {

	private FakePortfolioRepository portfolioRepo;
	private FakePositionRepository positionRepo;
	private FakeMarketPriceRepository marketPriceRepo;
	private PortfolioValueService service;

	@BeforeEach
	void setUp() {
		portfolioRepo = new FakePortfolioRepository();
		positionRepo = new FakePositionRepository();
		marketPriceRepo = new FakeMarketPriceRepository();
		service = new PortfolioValueService(portfolioRepo, positionRepo, marketPriceRepo);
	}

	@Test
	void returnsZeroWhenPortfolioHasNoPositions() {
		UUID investorId = UUID.randomUUID();
		portfolioRepo.seed(portfolio(investorId));

		assertEquals(BigDecimal.ZERO, service.getPortfolioValue(investorId).totalValue());
	}

	@Test
	void calculatesTotalValueAsSumOfQuantityTimesPrice() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		portfolioRepo.seed(portfolio);
		positionRepo.seed(position(portfolio.getId(), "AAPL", 10));
		positionRepo.seed(position(portfolio.getId(), "MSFT", 5));
		marketPriceRepo.stubLatestPrice("AAPL", new BigDecimal("150.00"));
		marketPriceRepo.stubLatestPrice("MSFT", new BigDecimal("300.00"));

		// AAPL: 10 × 150 = 1500, MSFT: 5 × 300 = 1500, total = 3000
		assertEquals(new BigDecimal("3000.00"), service.getPortfolioValue(investorId).totalValue());
	}

	@Test
	void excludesPositionsWithNoStoredPrice() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		portfolioRepo.seed(portfolio);
		positionRepo.seed(position(portfolio.getId(), "AAPL", 10));
		positionRepo.seed(position(portfolio.getId(), "UNKNOWN", 5));
		marketPriceRepo.stubLatestPrice("AAPL", new BigDecimal("100.00"));

		assertEquals(new BigDecimal("1000.00"), service.getPortfolioValue(investorId).totalValue());
	}

	@Test
	void throwsWhenNoPortfolioFound() {
		assertThrows(IllegalStateException.class, () -> service.getPortfolioValue(UUID.randomUUID()));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId, String ticker, int quantity) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(quantity);
		p.setOperationDate(LocalDate.of(2024, 1, 1));
		return p;
	}
}

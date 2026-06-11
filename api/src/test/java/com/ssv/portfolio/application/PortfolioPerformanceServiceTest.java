package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.market.domain.MarketPrice;
import com.ssv.market.domain.MarketPriceCreateRequest;
import com.ssv.market.fake.FakeMarketPriceRepository;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class PortfolioPerformanceServiceTest {

	private FakePortfolioRepository portfolioRepo;
	private FakePositionRepository positionRepo;
	private FakeMarketPriceRepository marketPriceRepo;
	private PortfolioPerformanceService service;

	@BeforeEach
	void setUp() {
		portfolioRepo = new FakePortfolioRepository();
		positionRepo = new FakePositionRepository();
		marketPriceRepo = new FakeMarketPriceRepository();
		service = new PortfolioPerformanceService(portfolioRepo, positionRepo, marketPriceRepo);
	}

	@Test
	void returnsZeroWhenPortfolioHasNoPositions() {
		UUID investorId = UUID.randomUUID();
		portfolioRepo.seed(portfolio(investorId));

		assertEquals(BigDecimal.ZERO, service.getPortfolioPerformance(investorId).totalValue());
		assertEquals(BigDecimal.ZERO, service.getPortfolioPerformance(investorId).totalPnL());
	}

	@Test
	void calculatesTotalValueAndPnLUsingStoredPrices() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		portfolioRepo.seed(portfolio);
		positionRepo.seed(position(portfolio.getId(), "AAPL", 10, LocalDate.of(2024, 1, 1)));
		// historical price at operation date: 100.00
		MarketPriceCreateRequest historicalReq = new MarketPriceCreateRequest("AAPL", new BigDecimal("100.00"), "USD",
				Instant.parse("2024-01-01T00:00:00Z"), "yahoo");
		marketPriceRepo.save(new MarketPrice(historicalReq));
		// latest price: 150.00
		marketPriceRepo.stubLatestPrice("AAPL", new BigDecimal("150.00"));

		// totalValue = 10 * 150 = 1500
		// totalCost = 10 * 100 = 1000
		// totalPnL = 500
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

	private static Position position(UUID portfolioId, String ticker, int quantity, LocalDate opDate) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(quantity);
		p.setOperationDate(opDate);
		return p;
	}
}

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
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class UpdatePositionServiceTest {

	private FakePortfolioRepository fakePortfolioRepo;
	private FakePositionRepository fakePositionRepo;
	private FakeCurrentPriceProvider priceProvider;
	private PortfolioService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakePositionRepo = new FakePositionRepository();
		priceProvider = new FakeCurrentPriceProvider();
		service = new PortfolioService(fakePortfolioRepo, fakePositionRepo, priceProvider);
	}

	@Test
	void updatesAndReturnsPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position existing = position(portfolio.getId(), "AAPL", 10, LocalDate.of(2024, 1, 1));
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(existing);
		priceProvider.stub("MSFT", new BigDecimal("300.00"));
		AddPositionRequest request = new AddPositionRequest("MSFT", 20, LocalDate.of(2024, 6, 1));

		PositionResponse response = service.updatePosition(investorId, existing.getId(), request);

		assertEquals(existing.getId(), response.id());
		assertEquals("MSFT", response.ticker());
		assertEquals(20, response.quantity());
		assertEquals(LocalDate.of(2024, 6, 1), response.operationDate());
		// cost basis re-priced at current market: 20 × 300 = 6000
		assertEquals(new BigDecimal("6000.00"), fakePositionRepo.lastSaved().getCostBasis());
	}

	@Test
	void throwsNotFoundWhenPositionBelongsToAnotherInvestor() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();
		fakePortfolioRepo.seed(portfolio);
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now());

		assertThrows(PositionNotFoundException.class, () -> service.updatePosition(investorId, positionId, request));
	}

	@Test
	void throwsNotFoundWhenPositionDoesNotExist() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();
		fakePortfolioRepo.seed(portfolio);
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now());

		assertThrows(PositionNotFoundException.class, () -> service.updatePosition(investorId, positionId, request));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId, String ticker, int qty, LocalDate date) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(qty);
		p.setOperationDate(date);
		return p;
	}
}

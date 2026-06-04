package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class RemovePositionServiceTest {

	private FakePortfolioRepository fakePortfolioRepo;
	private FakePositionRepository fakePositionRepo;
	private PortfolioService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakePositionRepo = new FakePositionRepository();
		service = new PortfolioService(fakePortfolioRepo, fakePositionRepo);
	}

	@Test
	void deletesPositionWhenItBelongsToInvestor() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId());
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(position);

		service.removePosition(investorId, position.getId());

		assertTrue(fakePositionRepo.wasDeleted(position));
	}

	@Test
	void throwsNotFoundWhenPositionDoesNotExist() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();
		fakePortfolioRepo.seed(portfolio);

		assertThrows(PositionNotFoundException.class, () -> service.removePosition(investorId, positionId));
		assertTrue(fakePositionRepo.deletedPositions().isEmpty());
	}

	@Test
	void throwsNotFoundWhenPositionBelongsToAnotherInvestor() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();
		fakePortfolioRepo.seed(portfolio);

		assertThrows(PositionNotFoundException.class, () -> service.removePosition(investorId, positionId));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker("AAPL");
		p.setQuantity(10);
		p.setOperationDate(LocalDate.of(2024, 1, 1));
		return p;
	}
}

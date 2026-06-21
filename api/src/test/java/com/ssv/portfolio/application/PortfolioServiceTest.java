package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.company.fake.FakeCompanyProvisioningService;
import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class PortfolioServiceTest {

	private FakePortfolioRepository fakePortfolioRepo;
	private FakePositionRepository fakePositionRepo;
	private PortfolioService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakePositionRepo = new FakePositionRepository();
		service = new PortfolioService(fakePortfolioRepo, fakePositionRepo, new FakeCurrentPriceProvider(),
				new FakeCompanyProvisioningService());
	}

	@Test
	void returnsPortfolioWithPositions() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId());
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(position);

		PortfolioResponse response = service.getPortfolio(investorId);

		assertEquals(portfolio.getId(), response.id());
		assertEquals(1, response.positions().size());
		assertEquals(position.getId(), response.positions().get(0).id());
		assertEquals("AAPL", response.positions().get(0).ticker());
		assertEquals(10, response.positions().get(0).quantity());
		assertEquals(LocalDate.of(2024, 1, 15), response.positions().get(0).operationDate());
	}

	@Test
	void returnsEmptyPositionsWhenPortfolioHasNone() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		PortfolioResponse response = service.getPortfolio(investorId);

		assertEquals(portfolio.getId(), response.id());
		assertTrue(response.positions().isEmpty());
	}

	@Test
	void throwsWhenNoPortfolioFound() {
		UUID investorId = UUID.randomUUID();

		assertThrows(IllegalStateException.class, () -> service.getPortfolio(investorId));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId) {
		Position pos = new Position();
		pos.setId(UUID.randomUUID());
		pos.setPortfolioId(portfolioId);
		pos.setTicker("AAPL");
		pos.setQuantity(10);
		pos.setOperationDate(LocalDate.of(2024, 1, 15));
		return pos;
	}
}

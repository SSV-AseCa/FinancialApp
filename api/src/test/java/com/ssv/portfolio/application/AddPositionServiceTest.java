package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class AddPositionServiceTest {

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
	void createsAndReturnsNewPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);
		AddPositionRequest request = new AddPositionRequest("AAPL", 10, LocalDate.of(2024, 1, 15),
				java.math.BigDecimal.valueOf(120.50));

		PositionResponse response = service.addPosition(investorId, request);

		assertNotNull(response.id());
		assertEquals("AAPL", response.ticker());
		assertEquals(10, response.quantity());
		assertEquals(LocalDate.of(2024, 1, 15), response.operationDate());
	}

	@Test
	void throwsWhenNoPortfolioFoundForInvestor() {
		UUID investorId = UUID.randomUUID();
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now(), java.math.BigDecimal.ZERO);

		assertThrows(IllegalStateException.class, () -> service.addPosition(investorId, request));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}
}

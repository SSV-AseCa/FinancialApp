package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class AddPositionServiceTest {

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
	void createsAndReturnsNewPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);
		priceProvider.stub("AAPL", new BigDecimal("150.00"));
		AddPositionRequest request = new AddPositionRequest("AAPL", 10, LocalDate.of(2024, 1, 15));

		PositionResponse response = service.addPosition(investorId, request);

		assertNotNull(response.id());
		assertEquals("AAPL", response.ticker());
		assertEquals(10, response.quantity());
		assertEquals(LocalDate.of(2024, 1, 15), response.operationDate());
		// cost basis captured at current market price: 10 × 150 = 1500
		assertEquals(new BigDecimal("1500.00"), fakePositionRepo.lastSaved().getCostBasis());
	}

	@Test
	void throwsWhenNoPortfolioFoundForInvestor() {
		UUID investorId = UUID.randomUUID();
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now());

		assertThrows(IllegalStateException.class, () -> service.addPosition(investorId, request));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}
}

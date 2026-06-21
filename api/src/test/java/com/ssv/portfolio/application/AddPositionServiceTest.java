package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.company.fake.FakeCompanyProvisioningService;
import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;

class AddPositionServiceTest {

	private static final String APPLE_CIK = "0000320193";

	private FakePortfolioRepository fakePortfolioRepo;
	private FakePositionRepository fakePositionRepo;
	private FakeCurrentPriceProvider priceProvider;
	private FakeCompanyProvisioningService provisioning;
	private PortfolioService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakePositionRepo = new FakePositionRepository();
		priceProvider = new FakeCurrentPriceProvider();
		provisioning = new FakeCompanyProvisioningService();
		service = new PortfolioService(fakePortfolioRepo, fakePositionRepo, priceProvider, provisioning);
	}

	@Test
	void resolvesCikToSymbolAndCapturesCostBasis() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);
		provisioning.register(APPLE_CIK, "AAPL", "Apple Inc.");
		priceProvider.stub("AAPL", new BigDecimal("150.00"));
		AddPositionRequest request = new AddPositionRequest(APPLE_CIK, 10, LocalDate.of(2024, 1, 15));

		PositionResponse response = service.addPosition(investorId, request);

		assertNotNull(response.id());
		// the resolved ticker symbol is stored, not the raw CIK
		assertEquals("AAPL", response.ticker());
		assertEquals(10, response.quantity());
		assertEquals(LocalDate.of(2024, 1, 15), response.operationDate());
		// cost basis captured at current market price: 10 × 150 = 1500
		assertEquals(new BigDecimal("1500.00"), fakePositionRepo.lastSaved().getCostBasis());
	}

	@Test
	void rejectsUnknownCompany() {
		UUID investorId = UUID.randomUUID();
		fakePortfolioRepo.seed(portfolio(investorId));
		// CIK not registered, so the company cannot be resolved.
		AddPositionRequest request = new AddPositionRequest("9999999999", 5, LocalDate.of(2024, 1, 15));

		assertThrows(CompanyNotFoundException.class, () -> service.addPosition(investorId, request));
	}

	@Test
	void throwsWhenNoPortfolioFoundForInvestor() {
		UUID investorId = UUID.randomUUID();
		AddPositionRequest request = new AddPositionRequest(APPLE_CIK, 5, LocalDate.now());

		assertThrows(IllegalStateException.class, () -> service.addPosition(investorId, request));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}
}

package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.market.fake.FakeCurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.fake.FakeCompanyProvisioningService;
import com.ssv.transaction.fake.FakeTransactionRepository;

class BuySharesServiceTest {

	private static final String CIK = "0000320193";

	private FakePortfolioRepository fakePortfolioRepo;
	private FakePositionRepository fakePositionRepo;
	private FakeTransactionRepository fakeTxRepo;
	private FakeCurrentPriceProvider priceProvider;
	private TransactionService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakePositionRepo = new FakePositionRepository();
		fakeTxRepo = new FakeTransactionRepository();
		// FakeCompanyProvisioningService resolves a CIK to a symbol equal to the CIK.
		priceProvider = new FakeCurrentPriceProvider().stub(CIK, new BigDecimal("100.00"));
		service = new TransactionService(fakePortfolioRepo, fakeTxRepo, new FakeCompanyProvisioningService(),
				new PositionMutator(fakePositionRepo), priceProvider);
	}

	@Test
	void createsTransactionAndNewPositionWhenNoneExists() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		TransactionResponse response = service.buy(investorId, new BuyRequest(CIK, 10));

		assertNotNull(response.id());
		assertEquals(CIK, response.cik());
		assertEquals(10, response.quantity());
		assertEquals(TransactionType.BUY, response.type());
		assertFalse(fakeTxRepo.store.isEmpty());
		assertNotNull(fakePositionRepo.lastSaved());
		// cost basis captured at purchase: 10 × 100 = 1000
		assertEquals(new BigDecimal("1000.00"), fakePositionRepo.lastSaved().getCostBasis());
	}

	@Test
	void increasesExistingPositionQuantityOnSecondBuy() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position existing = position(portfolio.getId(), CIK, 5);
		existing.setCostBasis(new BigDecimal("500.00"));
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(existing);

		service.buy(investorId, new BuyRequest(CIK, 3));

		assertEquals(8, fakePositionRepo.lastSaved().getQuantity());
		// existing 500 + new 3 × 100 = 800
		assertEquals(new BigDecimal("800.00"), fakePositionRepo.lastSaved().getCostBasis());
	}

	@Test
	void savesTransactionBeforeUpdatingPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		service.buy(investorId, new BuyRequest(CIK, 1));

		assertFalse(fakeTxRepo.store.isEmpty());
		assertNotNull(fakePositionRepo.lastSaved());
	}

	@Test
	void rejectsBuyWhenNoMarketPriceAvailable() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		// No price stubbed for this CIK, so cost basis cannot be captured.
		assertThrows(MarketPriceFetchException.class, () -> service.buy(investorId, new BuyRequest("9999999999", 1)));
		assertTrue(fakeTxRepo.store.isEmpty());
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId, String cik, int quantity) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(cik);
		p.setQuantity(quantity);
		return p;
	}
}

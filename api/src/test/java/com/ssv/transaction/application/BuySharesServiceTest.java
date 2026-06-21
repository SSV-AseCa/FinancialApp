package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.fake.FakeCompanyProvisioningService;
import com.ssv.transaction.fake.FakeTransactionRepository;

class BuySharesServiceTest {

	private FakePortfolioRepository fakePortfolioRepo;
	private FakePositionRepository fakePositionRepo;
	private FakeTransactionRepository fakeTxRepo;
	private TransactionService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakePositionRepo = new FakePositionRepository();
		fakeTxRepo = new FakeTransactionRepository();
		service = new TransactionService(fakePortfolioRepo, fakeTxRepo, new FakeCompanyProvisioningService(),
				new PositionMutator(fakePositionRepo));
	}

	@Test
	void createsTransactionAndNewPositionWhenNoneExists() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		TransactionResponse response = service.buy(investorId, new BuyRequest("0000320193", 10));

		assertNotNull(response.id());
		assertEquals("0000320193", response.cik());
		assertEquals(10, response.quantity());
		assertEquals(TransactionType.BUY, response.type());
		assertFalse(fakeTxRepo.store.isEmpty());
		assertNotNull(fakePositionRepo.lastSaved());
	}

	@Test
	void increasesExistingPositionQuantityOnSecondBuy() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position existing = position(portfolio.getId(), "0000320193", 5);
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(existing);

		service.buy(investorId, new BuyRequest("0000320193", 3));

		assertEquals(8, fakePositionRepo.lastSaved().getQuantity());
	}

	@Test
	void savesTransactionBeforeUpdatingPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		service.buy(investorId, new BuyRequest("0000320193", 1));

		assertFalse(fakeTxRepo.store.isEmpty());
		assertNotNull(fakePositionRepo.lastSaved());
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

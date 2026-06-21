package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.SellRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.exceptions.BusinessRuleException;
import com.ssv.transaction.fake.FakeCompanyProvisioningService;
import com.ssv.transaction.fake.FakeTransactionRepository;

class SellSharesServiceTest {

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
	void createsTransactionAndRemovesPositionWhenSellingAll() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId(), "0000320193", 10);
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(position);

		TransactionResponse response = service.sell(investorId, new SellRequest("0000320193", 10));

		assertEquals(TransactionType.SELL, response.type());
		assertEquals(10, response.quantity());
		assertTrue(fakePositionRepo.wasDeleted(position));
	}

	@Test
	void decreasesPositionQuantityWhenSellingPartially() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId(), "0000320193", 10);
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(position);

		service.sell(investorId, new SellRequest("0000320193", 3));

		assertEquals(7, fakePositionRepo.lastSaved().getQuantity());
	}

	@Test
	void throws422WhenInvestorHasNoPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		assertThrows(BusinessRuleException.class, () -> service.sell(investorId, new SellRequest("0000320193", 5)));
		assertTrue(fakeTxRepo.store.isEmpty());
		assertTrue(fakePositionRepo.deletedPositions().isEmpty());
	}

	@Test
	void throws422WhenInsufficientShares() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId(), "0000320193", 3);
		fakePortfolioRepo.seed(portfolio);
		fakePositionRepo.seed(position);

		BusinessRuleException ex = assertThrows(BusinessRuleException.class,
				() -> service.sell(investorId, new SellRequest("0000320193", 5)));

		assertEquals("Insufficient shares: holds 3 but requested 5", ex.getMessage());
		assertTrue(fakePositionRepo.deletedPositions().isEmpty());
		assertTrue(fakePositionRepo.lastSaved() == null || fakePositionRepo.lastSaved().equals(position));
	}

	@Test
	void throws422WhenSellingSharesNotOwned() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		assertThrows(BusinessRuleException.class, () -> service.sell(investorId, new SellRequest("9999999999", 1)));
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

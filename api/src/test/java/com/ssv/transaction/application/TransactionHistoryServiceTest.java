package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.fake.FakeTransactionRepository;

class TransactionHistoryServiceTest {

	private FakePortfolioRepository fakePortfolioRepo;
	private FakeTransactionRepository fakeTxRepo;
	private TransactionHistoryService service;

	@BeforeEach
	void setUp() {
		fakePortfolioRepo = new FakePortfolioRepository();
		fakeTxRepo = new FakeTransactionRepository();
		service = new TransactionHistoryService(fakePortfolioRepo, fakeTxRepo);
	}

	@Test
	void returnsTransactionsNewestFirst() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);
		fakeTxRepo.store.add(transaction(portfolio.getId(), LocalDate.of(2024, 6, 1), TransactionType.SELL));
		fakeTxRepo.store.add(transaction(portfolio.getId(), LocalDate.of(2024, 1, 1), TransactionType.BUY));

		List<TransactionResponse> result = service.getHistory(investorId);

		assertEquals(2, result.size());
		assertEquals(LocalDate.of(2024, 6, 1), result.get(0).transactionDate());
		assertEquals(LocalDate.of(2024, 1, 1), result.get(1).transactionDate());
	}

	@Test
	void returnsEmptyListWhenNoTransactions() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);

		assertTrue(service.getHistory(investorId).isEmpty());
	}

	@Test
	void mapsAllFieldsCorrectly() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		fakePortfolioRepo.seed(portfolio);
		Transaction tx = transaction(portfolio.getId(), LocalDate.of(2024, 3, 15), TransactionType.BUY);
		fakeTxRepo.store.add(tx);

		TransactionResponse response = service.getHistory(investorId).get(0);

		assertEquals(tx.getId(), response.id());
		assertEquals(tx.getCik(), response.cik());
		assertEquals(tx.getQuantity(), response.quantity());
		assertEquals(TransactionType.BUY, response.type());
		assertEquals(LocalDate.of(2024, 3, 15), response.transactionDate());
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Transaction transaction(UUID portfolioId, LocalDate date, TransactionType type) {
		Transaction t = new Transaction();
		t.setId(UUID.randomUUID());
		t.setPortfolioId(portfolioId);
		t.setCik("0000320193");
		t.setQuantity(5);
		t.setType(type);
		t.setTransactionDate(date);
		return t;
	}
}

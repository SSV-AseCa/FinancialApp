package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

class TransactionHistoryServiceTest {

	private PortfolioRepository portfolioRepository;
	private TransactionRepository transactionRepository;
	private TransactionHistoryService service;

	@BeforeEach
	void setUp() {
		portfolioRepository = mock(PortfolioRepository.class);
		transactionRepository = mock(TransactionRepository.class);
		service = new TransactionHistoryService(portfolioRepository, transactionRepository);
	}

	@Test
	void returnsTransactionsNewestFirst() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(transactionRepository.findByPortfolioIdOrderByTransactionDateDescCreatedAtDesc(portfolio.getId()))
				.thenReturn(List.of(transaction(portfolio.getId(), LocalDate.of(2024, 6, 1), TransactionType.SELL),
						transaction(portfolio.getId(), LocalDate.of(2024, 1, 1), TransactionType.BUY)));

		List<TransactionResponse> result = service.getHistory(investorId);

		assertEquals(2, result.size());
		assertEquals(LocalDate.of(2024, 6, 1), result.get(0).transactionDate());
		assertEquals(LocalDate.of(2024, 1, 1), result.get(1).transactionDate());
	}

	@Test
	void returnsEmptyListWhenNoTransactions() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(transactionRepository.findByPortfolioIdOrderByTransactionDateDescCreatedAtDesc(portfolio.getId()))
				.thenReturn(List.of());

		assertTrue(service.getHistory(investorId).isEmpty());
	}

	@Test
	void mapsAllFieldsCorrectly() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Transaction tx = transaction(portfolio.getId(), LocalDate.of(2024, 3, 15), TransactionType.BUY);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(transactionRepository.findByPortfolioIdOrderByTransactionDateDescCreatedAtDesc(portfolio.getId()))
				.thenReturn(List.of(tx));

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

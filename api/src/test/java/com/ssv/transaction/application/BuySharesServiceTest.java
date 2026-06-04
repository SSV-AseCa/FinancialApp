package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

class BuySharesServiceTest {

	private PortfolioRepository portfolioRepository;
	private PositionRepository positionRepository;
	private TransactionRepository transactionRepository;
	private TransactionService service;

	@BeforeEach
	void setUp() {
		portfolioRepository = mock(PortfolioRepository.class);
		positionRepository = mock(PositionRepository.class);
		transactionRepository = mock(TransactionRepository.class);
		service = new TransactionService(portfolioRepository, positionRepository, transactionRepository);
	}

	@Test
	void createsTransactionAndNewPositionWhenNoneExists() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "0000320193"))
				.thenReturn(Optional.empty());
		when(transactionRepository.save(any())).thenAnswer(i -> {
			Transaction t = i.getArgument(0);
			t.setId(UUID.randomUUID());
			return t;
		});
		when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		TransactionResponse response = service.buy(investorId, new BuyRequest("0000320193", 10));

		assertNotNull(response.id());
		assertEquals("0000320193", response.cik());
		assertEquals(10, response.quantity());
		assertEquals(TransactionType.BUY, response.type());
		verify(transactionRepository).save(any(Transaction.class));
		verify(positionRepository).save(any(Position.class));
	}

	@Test
	void increasesExistingPositionQuantityOnSecondBuy() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position existing = position(portfolio.getId(), "0000320193", 5);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "0000320193"))
				.thenReturn(Optional.of(existing));
		when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		service.buy(investorId, new BuyRequest("0000320193", 3));

		ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
		verify(positionRepository).save(captor.capture());
		assertEquals(8, captor.getValue().getQuantity());
	}

	@Test
	void savesTransactionBeforeUpdatingPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(any(), any())).thenReturn(Optional.empty());
		when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		service.buy(investorId, new BuyRequest("0000320193", 1));

		verify(transactionRepository, times(1)).save(any());
		verify(positionRepository, times(1)).save(any());
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

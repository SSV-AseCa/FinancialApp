package com.ssv.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.ssv.transaction.dto.SellRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.exceptions.BusinessRuleException;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

class SellSharesServiceTest {

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
	void createsTransactionAndRemovesPositionWhenSellingAll() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId(), "0000320193", 10);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "0000320193"))
				.thenReturn(Optional.of(position));
		when(transactionRepository.save(any())).thenAnswer(i -> {
			Transaction t = i.getArgument(0);
			t.setId(UUID.randomUUID());
			return t;
		});

		TransactionResponse response = service.sell(investorId, new SellRequest("0000320193", 10));

		assertEquals(TransactionType.SELL, response.type());
		assertEquals(10, response.quantity());
		verify(positionRepository).delete(position);
	}

	@Test
	void decreasesPositionQuantityWhenSellingPartially() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId(), "0000320193", 10);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "0000320193"))
				.thenReturn(Optional.of(position));
		when(transactionRepository.save(any())).thenAnswer(i -> {
			Transaction t = i.getArgument(0);
			t.setId(UUID.randomUUID());
			return t;
		});
		when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		service.sell(investorId, new SellRequest("0000320193", 3));

		ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
		verify(positionRepository).save(captor.capture());
		assertEquals(7, captor.getValue().getQuantity());
	}

	@Test
	void throws422WhenInvestorHasNoPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "0000320193"))
				.thenReturn(Optional.empty());

		assertThrows(BusinessRuleException.class, () -> service.sell(investorId, new SellRequest("0000320193", 5)));
		verify(transactionRepository, never()).save(any());
		verify(positionRepository, never()).delete(any());
	}

	@Test
	void throws422WhenInsufficientShares() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId(), "0000320193", 3);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "0000320193"))
				.thenReturn(Optional.of(position));

		BusinessRuleException ex = assertThrows(BusinessRuleException.class,
				() -> service.sell(investorId, new SellRequest("0000320193", 5)));

		assertEquals("Insufficient shares: holds 3 but requested 5", ex.getMessage());
		verify(positionRepository, never()).delete(any());
		verify(positionRepository, never()).save(any());
	}

	@Test
	void throws422WhenSellingSharesNotOwned() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), "9999999999"))
				.thenReturn(Optional.empty());

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

package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

class RemovePositionServiceTest {

	private PortfolioRepository portfolioRepository;
	private PositionRepository positionRepository;
	private PortfolioService service;

	@BeforeEach
	void setUp() {
		portfolioRepository = mock(PortfolioRepository.class);
		positionRepository = mock(PositionRepository.class);
		service = new PortfolioService(portfolioRepository, positionRepository);
	}

	@Test
	void deletesPositionWhenItBelongsToInvestor() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId());

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByIdAndPortfolioId(position.getId(), portfolio.getId()))
				.thenReturn(Optional.of(position));

		service.removePosition(investorId, position.getId());

		verify(positionRepository).delete(position);
	}

	@Test
	void throwsNotFoundWhenPositionDoesNotExist() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())).thenReturn(Optional.empty());

		assertThrows(PositionNotFoundException.class, () -> service.removePosition(investorId, positionId));
		verify(positionRepository, never()).delete(position(portfolio.getId()));
	}

	@Test
	void throwsNotFoundWhenPositionBelongsToAnotherInvestor() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())).thenReturn(Optional.empty());

		assertThrows(PositionNotFoundException.class, () -> service.removePosition(investorId, positionId));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker("AAPL");
		p.setQuantity(10);
		p.setOperationDate(LocalDate.of(2024, 1, 1));
		return p;
	}
}

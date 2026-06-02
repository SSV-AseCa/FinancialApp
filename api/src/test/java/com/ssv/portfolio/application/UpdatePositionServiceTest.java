package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.entity.Portfolio;
import com.ssv.entity.Position;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.repository.PortfolioRepository;
import com.ssv.repository.PositionRepository;

class UpdatePositionServiceTest {

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
	void updatesAndReturnsPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position existing = position(portfolio.getId(), "AAPL", 10, LocalDate.of(2024, 1, 1));
		AddPositionRequest request = new AddPositionRequest("MSFT", 20, LocalDate.of(2024, 6, 1));

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByIdAndPortfolioId(existing.getId(), portfolio.getId()))
				.thenReturn(Optional.of(existing));
		when(positionRepository.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

		PositionResponse response = service.updatePosition(investorId, existing.getId(), request);

		assertEquals(existing.getId(), response.id());
		assertEquals("MSFT", response.ticker());
		assertEquals(20, response.quantity());
		assertEquals(LocalDate.of(2024, 6, 1), response.operationDate());
	}

	@Test
	void throwsNotFoundWhenPositionBelongsToAnotherInvestor() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now());

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())).thenReturn(Optional.empty());

		assertThrows(PositionNotFoundException.class, () -> service.updatePosition(investorId, positionId, request));
	}

	@Test
	void throwsNotFoundWhenPositionDoesNotExist() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		UUID positionId = UUID.randomUUID();
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now());

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())).thenReturn(Optional.empty());

		assertThrows(PositionNotFoundException.class, () -> service.updatePosition(investorId, positionId, request));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId, String ticker, int qty, LocalDate date) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(qty);
		p.setOperationDate(date);
		return p;
	}
}

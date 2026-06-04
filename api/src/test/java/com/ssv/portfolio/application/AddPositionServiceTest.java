package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

class AddPositionServiceTest {

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
	void createsAndReturnsNewPosition() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		AddPositionRequest request = new AddPositionRequest("AAPL", 10, LocalDate.of(2024, 1, 15));

		Position saved = savedPosition(portfolio.getId(), request);
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.save(any(Position.class))).thenReturn(saved);

		PositionResponse response = service.addPosition(investorId, request);

		assertNotNull(response.id());
		assertEquals("AAPL", response.ticker());
		assertEquals(10, response.quantity());
		assertEquals(LocalDate.of(2024, 1, 15), response.operationDate());
	}

	@Test
	void throwsWhenNoPortfolioFoundForInvestor() {
		UUID investorId = UUID.randomUUID();
		AddPositionRequest request = new AddPositionRequest("AAPL", 5, LocalDate.now());
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.empty());

		assertThrows(IllegalStateException.class, () -> service.addPosition(investorId, request));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position savedPosition(UUID portfolioId, AddPositionRequest request) {
		Position p = new Position();
		p.setId(UUID.randomUUID());
		p.setPortfolioId(portfolioId);
		p.setTicker(request.ticker());
		p.setQuantity(request.quantity());
		p.setOperationDate(request.operationDate());
		return p;
	}
}

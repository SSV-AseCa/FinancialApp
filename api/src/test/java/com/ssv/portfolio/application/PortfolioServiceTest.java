package com.ssv.portfolio.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

class PortfolioServiceTest {

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
	void returnsPortfolioWithPositions() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);
		Position position = position(portfolio.getId());

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioId(portfolio.getId())).thenReturn(List.of(position));

		PortfolioResponse response = service.getPortfolio(investorId);

		assertEquals(portfolio.getId(), response.id());
		assertEquals(1, response.positions().size());
		assertEquals(position.getId(), response.positions().get(0).id());
		assertEquals("AAPL", response.positions().get(0).ticker());
		assertEquals(10, response.positions().get(0).quantity());
		assertEquals(LocalDate.of(2024, 1, 15), response.positions().get(0).operationDate());
	}

	@Test
	void returnsEmptyPositionsWhenPortfolioHasNone() {
		UUID investorId = UUID.randomUUID();
		Portfolio portfolio = portfolio(investorId);

		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.of(portfolio));
		when(positionRepository.findByPortfolioId(portfolio.getId())).thenReturn(List.of());

		PortfolioResponse response = service.getPortfolio(investorId);

		assertEquals(portfolio.getId(), response.id());
		assertTrue(response.positions().isEmpty());
	}

	@Test
	void throwsWhenNoPortfolioFound() {
		UUID investorId = UUID.randomUUID();
		when(portfolioRepository.findByInvestorId(investorId)).thenReturn(Optional.empty());

		assertThrows(IllegalStateException.class, () -> service.getPortfolio(investorId));
	}

	private static Portfolio portfolio(UUID investorId) {
		Portfolio p = new Portfolio();
		p.setId(UUID.randomUUID());
		p.setInvestorId(investorId);
		return p;
	}

	private static Position position(UUID portfolioId) {
		Position pos = new Position();
		pos.setId(UUID.randomUUID());
		pos.setPortfolioId(portfolioId);
		pos.setTicker("AAPL");
		pos.setQuantity(10);
		pos.setOperationDate(LocalDate.of(2024, 1, 15));
		return pos;
	}
}

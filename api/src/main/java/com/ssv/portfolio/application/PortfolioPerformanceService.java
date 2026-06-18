package com.ssv.portfolio.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.market.infrastructure.persistence.MarketPriceRepository;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.PortfolioPerformanceResponse;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioPerformanceService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final MarketPriceRepository marketPriceRepository;

	public PortfolioPerformanceResponse getPortfolioPerformance(UUID investorId) {
		Portfolio portfolio = requirePortfolio(investorId);
		List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId());

		BigDecimal totalValue = computeTotalValue(positions);
		BigDecimal totalCost = computeTotalCost(positions);
		BigDecimal totalPnL = totalValue.subtract(totalCost);

		return new PortfolioPerformanceResponse(totalValue, totalPnL);
	}

	private Portfolio requirePortfolio(UUID investorId) {
		return portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
	}

	private BigDecimal computeTotalValue(List<Position> positions) {
		return positions.stream().map(p -> PortfolioValuationCalculator.currentValue(p, marketPriceRepository))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal computeTotalCost(List<Position> positions) {
		return positions.stream().map(p -> PortfolioValuationCalculator.costBasis(p, marketPriceRepository))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}

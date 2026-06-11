package com.ssv.portfolio.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.market.infrastructure.persistence.MarketPriceRepository;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.PortfolioValueResponse;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioValueService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final MarketPriceRepository marketPriceRepository;

	public PortfolioValueResponse getPortfolioValue(UUID investorId) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId());
		BigDecimal total = positions.stream().map(this::positionValue).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new PortfolioValueResponse(total);
	}

	private BigDecimal positionValue(Position position) {
		return PortfolioValuationCalculator.currentValue(position, marketPriceRepository);
	}
}

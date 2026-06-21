package com.ssv.portfolio.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.market.application.CurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.PortfolioValueResponse;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class PortfolioValueService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final CurrentPriceProvider currentPriceProvider;

	public PortfolioValueResponse getPortfolioValue(UUID investorId) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId());
		BigDecimal total = positions.stream().map(this::positionValue).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new PortfolioValueResponse(total);
	}

	private BigDecimal positionValue(Position position) {
		return PortfolioValuationCalculator.currentValue(position, currentPriceProvider);
	}
}

package com.ssv.portfolio.application;

import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Exposes the distinct ticker symbols the system actually holds, so the price-
 * update batch fetches prices for real positions. Reads the {@code position}
 * table (the single source of holdings), not the legacy
 * {@code portfolio_positions}.
 */
@Service
@RequiredArgsConstructor
public class PortfolioPositionQueryService {

	private final PositionRepository positionRepository;

	public List<String> findDistinctSymbols() {
		return positionRepository.findDistinctTickers();
	}
}

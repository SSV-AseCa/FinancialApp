package com.ssv.portfolio.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.entity.Portfolio;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.repository.PortfolioRepository;
import com.ssv.repository.PositionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;

	public PortfolioResponse getPortfolio(UUID investorId) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));

		List<PositionResponse> positions = positionRepository.findByPortfolioId(portfolio.getId()).stream()
				.map(p -> new PositionResponse(p.getId(), p.getTicker(), p.getQuantity(), p.getOperationDate()))
				.toList();

		return new PortfolioResponse(portfolio.getId(), positions);
	}
}

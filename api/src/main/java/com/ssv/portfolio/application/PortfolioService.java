package com.ssv.portfolio.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.entity.Portfolio;
import com.ssv.entity.Position;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
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

	public PositionResponse addPosition(UUID investorId, AddPositionRequest request) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		Position saved = positionRepository.save(buildPosition(portfolio.getId(), request));
		return new PositionResponse(saved.getId(), saved.getTicker(), saved.getQuantity(), saved.getOperationDate());
	}

	public PositionResponse updatePosition(UUID investorId, UUID positionId, AddPositionRequest request) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		Position position = positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())
				.orElseThrow(() -> new PositionNotFoundException(positionId));
		applyUpdate(position, request);
		Position saved = positionRepository.save(position);
		return new PositionResponse(saved.getId(), saved.getTicker(), saved.getQuantity(), saved.getOperationDate());
	}

	private void applyUpdate(Position position, AddPositionRequest request) {
		position.setTicker(request.ticker());
		position.setQuantity(request.quantity());
		position.setOperationDate(request.operationDate());
	}

	private Position buildPosition(UUID portfolioId, AddPositionRequest request) {
		Position position = new Position();
		position.setPortfolioId(portfolioId);
		position.setTicker(request.ticker());
		position.setQuantity(request.quantity());
		position.setOperationDate(request.operationDate());
		return position;
	}
}

package com.ssv.portfolio.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.market.application.HistoricalPriceProvider;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.ModifyPositionRequest;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;
import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class PortfolioService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final CompanyProvisioningService companyProvisioningService;
	private final HistoricalPriceProvider historicalPriceProvider;

	public PortfolioResponse getPortfolio(UUID investorId) {
		UUID portfolioId = requirePortfolioId(investorId);
		return new PortfolioResponse(portfolioId,
				positionRepository.findByPortfolioId(portfolioId).stream().map(this::toResponse).toList());
	}

	public PositionResponse addPosition(UUID investorId, AddPositionRequest request) {
		UUID portfolioId = requirePortfolioId(investorId);
		String symbol = resolveSymbol(request.cik());
		return toResponse(positionRepository.save(newPosition(portfolioId, symbol, request)));
	}

	public PositionResponse updatePosition(UUID investorId, UUID positionId, ModifyPositionRequest request) {
		Position position = positionRepository.findByIdAndPortfolioId(positionId, requirePortfolioId(investorId))
				.orElseThrow(() -> new PositionNotFoundException(positionId));
		position.setQuantity(request.quantity());
		position.setCostBasis(PortfolioValuationCalculator.costBasisAt(historicalPriceProvider, position.getTicker(),
				request.quantity(), request.operationDate()));
		position.setOperationDate(request.operationDate());
		return toResponse(positionRepository.save(position));
	}

	public void removePosition(UUID investorId, UUID positionId) {
		Position position = positionRepository.findByIdAndPortfolioId(positionId, requirePortfolioId(investorId))
				.orElseThrow(() -> new PositionNotFoundException(positionId));
		positionRepository.delete(position);
	}

	private UUID requirePortfolioId(UUID investorId) {
		var portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		return portfolio.getId();
	}

	/** Validates the company exists and resolves its CIK to the ticker symbol. */
	private String resolveSymbol(String cik) {
		return companyProvisioningService.ensureCompany(cik).getSymbol();
	}

	private Position newPosition(UUID portfolioId, String symbol, AddPositionRequest request) {
		Position position = new Position();
		position.setPortfolioId(portfolioId);
		position.setTicker(symbol);
		position.setQuantity(request.quantity());
		position.setCostBasis(PortfolioValuationCalculator.costBasisAt(historicalPriceProvider, symbol,
				request.quantity(), request.operationDate()));
		position.setOperationDate(request.operationDate());
		return position;
	}

	private PositionResponse toResponse(Position position) {
		return new PositionResponse(position.getId(), position.getTicker(), position.getQuantity(),
				position.getOperationDate());
	}
}

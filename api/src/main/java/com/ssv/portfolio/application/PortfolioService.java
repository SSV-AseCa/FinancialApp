package com.ssv.portfolio.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.market.application.CurrentPriceProvider;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.AddPositionRequest;
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
	private final CurrentPriceProvider currentPriceProvider;
	private final CompanyProvisioningService companyProvisioningService;

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
		String symbol = resolveSymbol(request.cik());
		Position saved = positionRepository.save(buildPosition(portfolio.getId(), symbol, request));
		return new PositionResponse(saved.getId(), saved.getTicker(), saved.getQuantity(), saved.getOperationDate());
	}

	public PositionResponse updatePosition(UUID investorId, UUID positionId, AddPositionRequest request) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		Position position = positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())
				.orElseThrow(() -> new PositionNotFoundException(positionId));
		populate(position, resolveSymbol(request.cik()), request);
		Position saved = positionRepository.save(position);
		return new PositionResponse(saved.getId(), saved.getTicker(), saved.getQuantity(), saved.getOperationDate());
	}

	public void removePosition(UUID investorId, UUID positionId) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		Position position = positionRepository.findByIdAndPortfolioId(positionId, portfolio.getId())
				.orElseThrow(() -> new PositionNotFoundException(positionId));
		positionRepository.delete(position);
	}

	/** Validates the company exists and resolves its CIK to the ticker symbol. */
	private String resolveSymbol(String cik) {
		return companyProvisioningService.ensureCompany(cik).getSymbol();
	}

	private Position buildPosition(UUID portfolioId, String symbol, AddPositionRequest request) {
		Position position = new Position();
		position.setPortfolioId(portfolioId);
		return populate(position, symbol, request);
	}

	private Position populate(Position position, String symbol, AddPositionRequest request) {
		position.setTicker(symbol);
		position.setQuantity(request.quantity());
		position.setCostBasis(costBasisFor(symbol, request.quantity()));
		position.setOperationDate(request.operationDate());
		return position;
	}

	/**
	 * Cost basis for a manually recorded position is the current market price times
	 * the quantity. Best-effort: an unavailable price leaves it unset.
	 */
	private BigDecimal costBasisFor(String symbol, int quantity) {
		return currentPriceProvider.currentPrice(symbol).map(price -> price.multiply(BigDecimal.valueOf(quantity)))
				.orElse(null);
	}
}

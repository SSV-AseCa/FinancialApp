package com.ssv.portfolio.application;

import com.ssv.market.domain.MarketPrice;
import com.ssv.market.infrastructure.persistence.MarketPriceRepository;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.dto.PositionPnlResponse;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioPnlService {

	private static final BigDecimal ZERO = BigDecimal.ZERO;

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final MarketPriceRepository marketPriceRepository;

	public List<PositionPnlResponse> getPositionsPnl(UUID investorId) {
		var portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		return positionRepository.findByPortfolioId(portfolio.getId()).stream().map(this::toPnl).toList();
	}

	private PositionPnlResponse toPnl(Position position) {
		return marketPriceRepository.findTopBySymbolOrderByFetchedAtDesc(position.getTicker())
				.map(price -> pricedResponse(position, price)).orElseGet(() -> unpricedResponse(position));
	}

	private PositionPnlResponse pricedResponse(Position position, MarketPrice marketPrice) {
		BigDecimal quantity = BigDecimal.valueOf(position.getQuantity());
		BigDecimal currentPrice = marketPrice.getPrice();
		BigDecimal currentValue = currentPrice.multiply(quantity);
		BigDecimal pnl = currentPrice.subtract(costBasis(position)).multiply(quantity);
		return newResponse(position, currentPrice, currentValue, pnl);
	}

	private PositionPnlResponse unpricedResponse(Position position) {
		return newResponse(position, null, null, null);
	}

	private PositionPnlResponse newResponse(Position position, BigDecimal currentPrice, BigDecimal currentValue,
			BigDecimal pnl) {
		return new PositionPnlResponse(position.getId(), position.getTicker(), position.getQuantity(),
				position.getCostBasis(), currentPrice, currentValue, pnl);
	}

	private BigDecimal costBasis(Position position) {
		if (position.getCostBasis() == null) {
			return ZERO;
		}
		return position.getCostBasis();
	}
}

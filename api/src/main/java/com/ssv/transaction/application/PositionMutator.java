package com.ssv.transaction.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import com.ssv.transaction.exceptions.BusinessRuleException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

/**
 * Owns the state changes to a {@link Position} that a trade produces. Positions
 * are keyed by ticker symbol (the key market prices use), so callers pass an
 * already-resolved symbol. Cost basis is folded in at purchase time: a buy adds
 * {@code price x quantity}, a sell removes shares at the position's average
 * cost.
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
class PositionMutator {

	/** Scale used when apportioning average cost on a partial sell. */
	private static final int COST_SCALE = 4;

	private final PositionRepository positionRepository;

	void increase(UUID portfolioId, String symbol, int quantity, BigDecimal unitPrice) {
		BigDecimal addedCost = unitPrice.multiply(BigDecimal.valueOf(quantity));
		positionRepository.findByPortfolioIdAndTicker(portfolioId, symbol).ifPresentOrElse(position -> {
			position.setQuantity(position.getQuantity() + quantity);
			position.setCostBasis(currentCost(position).add(addedCost));
			positionRepository.save(position);
		}, () -> positionRepository.save(newPosition(portfolioId, symbol, quantity, addedCost)));
	}

	void decrease(UUID portfolioId, String symbol, int quantity, String cik) {
		Position position = positionRepository.findByPortfolioIdAndTicker(portfolioId, symbol)
				.orElseThrow(() -> new BusinessRuleException("No position found for CIK " + cik));
		if (position.getQuantity() < quantity) {
			throw new BusinessRuleException(
					"Insufficient shares: holds " + position.getQuantity() + " but requested " + quantity);
		}
		applyRemoval(position, quantity);
	}

	private void applyRemoval(Position position, int quantity) {
		if (position.getQuantity() == quantity) {
			positionRepository.delete(position);
		} else {
			position.setCostBasis(remainingCostAfterSale(position, quantity));
			position.setQuantity(position.getQuantity() - quantity);
			positionRepository.save(position);
		}
	}

	/**
	 * Average-cost reduction: the sold shares carry the position's average cost, so
	 * the remaining basis is proportional to the shares retained.
	 */
	private BigDecimal remainingCostAfterSale(Position position, int quantity) {
		BigDecimal cost = currentCost(position);
		int remaining = position.getQuantity() - quantity;
		return cost.multiply(BigDecimal.valueOf(remaining)).divide(BigDecimal.valueOf(position.getQuantity()),
				COST_SCALE, RoundingMode.HALF_UP);
	}

	private BigDecimal currentCost(Position position) {
		return position.getCostBasis() == null ? BigDecimal.ZERO : position.getCostBasis();
	}

	private Position newPosition(UUID portfolioId, String symbol, int quantity, BigDecimal costBasis) {
		Position position = new Position();
		position.setPortfolioId(portfolioId);
		position.setTicker(symbol);
		position.setQuantity(quantity);
		position.setCostBasis(costBasis);
		position.setOperationDate(LocalDate.now());
		return position;
	}
}

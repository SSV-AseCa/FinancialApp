package com.ssv.transaction.application;

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
 * already-resolved symbol.
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
class PositionMutator {

	private final PositionRepository positionRepository;

	void increase(UUID portfolioId, String symbol, int quantity) {
		positionRepository.findByPortfolioIdAndTicker(portfolioId, symbol).ifPresentOrElse(position -> {
			position.setQuantity(position.getQuantity() + quantity);
			positionRepository.save(position);
		}, () -> positionRepository.save(newPosition(portfolioId, symbol, quantity)));
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
			position.setQuantity(position.getQuantity() - quantity);
			positionRepository.save(position);
		}
	}

	private Position newPosition(UUID portfolioId, String symbol, int quantity) {
		Position position = new Position();
		position.setPortfolioId(portfolioId);
		position.setTicker(symbol);
		position.setQuantity(quantity);
		position.setOperationDate(LocalDate.now());
		return position;
	}
}

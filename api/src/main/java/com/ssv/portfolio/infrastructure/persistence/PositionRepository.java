package com.ssv.portfolio.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.portfolio.domain.Position;

public interface PositionRepository extends JpaRepository<Position, UUID> {
	List<Position> findByPortfolioId(UUID portfolioId);

	Optional<Position> findByIdAndPortfolioId(UUID id, UUID portfolioId);

	Optional<Position> findByPortfolioIdAndTicker(UUID portfolioId, String ticker);
}

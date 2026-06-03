package com.ssv.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.entity.Position;

public interface PositionRepository extends JpaRepository<Position, UUID> {
	List<Position> findByPortfolioId(UUID portfolioId);

	Optional<Position> findByIdAndPortfolioId(UUID id, UUID portfolioId);
}

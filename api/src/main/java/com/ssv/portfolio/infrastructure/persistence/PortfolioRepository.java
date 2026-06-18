package com.ssv.portfolio.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.portfolio.domain.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
	boolean existsByInvestorId(UUID investorId);

	Optional<Portfolio> findByInvestorId(UUID investorId);
}

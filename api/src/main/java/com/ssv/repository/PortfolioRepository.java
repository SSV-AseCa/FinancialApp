package com.ssv.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
	boolean existsByInvestorId(UUID investorId);
}

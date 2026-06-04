package com.ssv.portfolio.infrastructure.persistence;

import com.ssv.portfolio.domain.PortfolioPosition;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, UUID> {

	@Query("select distinct upper(position.symbol) from PortfolioPosition position")
	List<String> findDistinctSymbols();
}

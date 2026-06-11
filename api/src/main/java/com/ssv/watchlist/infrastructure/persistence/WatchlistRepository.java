package com.ssv.watchlist.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.watchlist.domain.WatchlistEntry;

public interface WatchlistRepository extends JpaRepository<WatchlistEntry, UUID> {
	boolean existsByInvestorIdAndCompanyId(UUID investorId, UUID companyId);
	Optional<WatchlistEntry> findByInvestorIdAndCompanyId(UUID investorId, UUID companyId);
}

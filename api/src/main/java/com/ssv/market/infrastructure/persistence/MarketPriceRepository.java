package com.ssv.market.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.market.domain.MarketPrice;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, UUID> {

	Optional<MarketPrice> findTopBySymbolOrderByFetchedAtDesc(String symbol);

	List<MarketPrice> findBySymbolOrderByFetchedAtDesc(String symbol);
}

package com.ssv.market.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.Instant;

import com.ssv.fake.JpaRepositoryBase;
import com.ssv.market.domain.MarketPrice;
import com.ssv.market.domain.MarketPriceCreateRequest;
import com.ssv.market.infrastructure.persistence.MarketPriceRepository;

public class FakeMarketPriceRepository extends JpaRepositoryBase<MarketPrice, UUID> implements MarketPriceRepository {

	private final List<MarketPrice> saved = new ArrayList<>();
	private final Map<String, MarketPrice> latestBySymbol = new HashMap<>();

	@Override
	public <S extends MarketPrice> S save(S entity) {
		saved.add(entity);
		return entity;
	}

	@Override
	public Optional<MarketPrice> findTopBySymbolOrderByFetchedAtDesc(String symbol) {
		return Optional.ofNullable(latestBySymbol.get(symbol));
	}

	@Override
	public List<MarketPrice> findBySymbolOrderByFetchedAtDesc(String symbol) {
		return saved.stream().filter(mp -> symbol.equals(mp.getSymbol())).toList();
	}

	public MarketPrice lastSaved() {
		return saved.get(saved.size() - 1);
	}

	public void stubLatest(String symbol, MarketPrice price) {
		latestBySymbol.put(symbol, price);
	}

	public void stubLatestPrice(String symbol, BigDecimal price) {
		MarketPriceCreateRequest request = new MarketPriceCreateRequest(symbol, price, "USD", Instant.now(), "yahoo");
		latestBySymbol.put(symbol, new MarketPrice(request));
	}
}

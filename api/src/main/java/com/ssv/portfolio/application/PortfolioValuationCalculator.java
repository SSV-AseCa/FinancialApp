package com.ssv.portfolio.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import com.ssv.market.domain.MarketPrice;
import com.ssv.market.infrastructure.persistence.MarketPriceRepository;
import com.ssv.portfolio.domain.Position;

public final class PortfolioValuationCalculator {

	private PortfolioValuationCalculator() {
	}

	public static BigDecimal currentValue(Position position, MarketPriceRepository marketPriceRepository) {
		Optional<MarketPrice> latest = marketPriceRepository.findTopBySymbolOrderByFetchedAtDesc(position.getTicker());
		return latest.map(mp -> mp.getPrice().multiply(BigDecimal.valueOf(position.getQuantity())))
				.orElse(BigDecimal.ZERO);
	}

	public static BigDecimal costBasis(Position position, MarketPriceRepository marketPriceRepository) {
		Instant cutoff = cutoffFor(position);
		if (cutoff == null) {
			return BigDecimal.ZERO;
		}

		int qty = position.getQuantity();
		return findHistoricalPrice(position, marketPriceRepository)
			.map(mp -> multiply(mp, qty))
			.orElse(BigDecimal.ZERO);
	}

	private static Optional<MarketPrice> findHistoricalPrice(Position position,
			MarketPriceRepository marketPriceRepository) {
		Instant cutoff = cutoffFor(position);
		return marketPriceRepository.findBySymbolOrderByFetchedAtDesc(position.getTicker()).stream()
			.filter(mp -> !mp.getFetchedAt().isAfter(cutoff)).findFirst();
	}

	private static Instant cutoffFor(Position position) {
		LocalDate opDate = position.getOperationDate();
		if (opDate == null) {
			return null;
		}
		return opDate.atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private static BigDecimal multiply(MarketPrice mp, int qty) {
		return mp.getPrice().multiply(BigDecimal.valueOf(qty));
	}
}

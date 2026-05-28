package com.ssv.market.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "market_prices", indexes = {
		@Index(name = "idx_market_price_symbol", columnList = "symbol"),
		@Index(name = "idx_market_price_fetched_at", columnList = "fetched_at"),
		@Index(name = "idx_market_price_symbol_fetched_at", columnList = "symbol,fetched_at")
})
@Getter
@NoArgsConstructor
public class MarketPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 10)
	private String symbol;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal price;

	@Column(nullable = false, length = 10)
	private String currency;

	@Column(name = "fetched_at", nullable = false)
	private Instant fetchedAt;

	@Column(nullable = false, length = 30)
	private String source;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();

	public MarketPrice(MarketPriceCreateRequest request) {
		symbol = request.symbol();
		price = request.price();
		currency = request.currency();
		fetchedAt = request.fetchedAt();
		source = request.source();
	}
}

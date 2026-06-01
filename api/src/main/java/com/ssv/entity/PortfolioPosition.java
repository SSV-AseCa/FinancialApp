package com.ssv.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "portfolio_positions")
@Getter
@NoArgsConstructor
public class PortfolioPosition {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "portfolio_id", nullable = false)
	private UUID portfolioId;

	@Column(nullable = false)
	private String symbol;

	@Column(nullable = false)
	private BigDecimal quantity;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	public PortfolioPosition(UUID portfolioId, String symbol, BigDecimal quantity) {
		this.portfolioId = portfolioId;
		this.symbol = symbol;
		this.quantity = quantity;
	}
}

package com.ssv.portfolio.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "position")
@Getter
@Setter
@NoArgsConstructor
public class Position {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "portfolio_id", nullable = false)
	private UUID portfolioId;

	@Column(nullable = false, length = 10)
	private String ticker;

	@Column(nullable = false)
	private Integer quantity;

	/**
	 * Running total cost basis (sum of executed price x quantity across buys,
	 * reduced by average cost on sells). Null for positions created before cost
	 * basis was tracked.
	 */
	@Column(name = "cost_basis")
	private BigDecimal costBasis;

	@Column(name = "operation_date", nullable = false)
	private LocalDate operationDate;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();
}

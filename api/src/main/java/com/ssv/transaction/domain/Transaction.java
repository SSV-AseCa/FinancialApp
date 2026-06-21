package com.ssv.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "portfolio_id", nullable = false)
	private UUID portfolioId;

	@Column(nullable = false, length = 10)
	private String cik;

	@Column(nullable = false)
	private Integer quantity;

	/**
	 * Executed unit price at the moment of the trade, captured from the market at
	 * transaction time. Null for transactions recorded before cost basis was
	 * tracked.
	 */
	@Column(name = "price")
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TransactionType type;

	@Column(name = "transaction_date", nullable = false)
	private LocalDate transactionDate;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();

	/** A buy trade, recording the unit price paid. */
	public static Transaction buy(UUID portfolioId, String cik, int quantity, BigDecimal price) {
		Transaction tx = base(portfolioId, cik, quantity, price);
		tx.type = TransactionType.BUY;
		return tx;
	}

	/**
	 * A sell trade, recording the unit price received (may be null if unavailable).
	 */
	public static Transaction sell(UUID portfolioId, String cik, int quantity, BigDecimal price) {
		Transaction tx = base(portfolioId, cik, quantity, price);
		tx.type = TransactionType.SELL;
		return tx;
	}

	private static Transaction base(UUID portfolioId, String cik, int quantity, BigDecimal price) {
		Transaction tx = new Transaction();
		tx.portfolioId = portfolioId;
		tx.cik = cik;
		tx.quantity = quantity;
		tx.price = price;
		tx.transactionDate = LocalDate.now();
		return tx;
	}
}

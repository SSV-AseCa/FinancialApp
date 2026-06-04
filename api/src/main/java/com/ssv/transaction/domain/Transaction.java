package com.ssv.transaction.domain;

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TransactionType type;

	@Column(name = "transaction_date", nullable = false)
	private LocalDate transactionDate;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();
}

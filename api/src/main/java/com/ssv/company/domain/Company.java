package com.ssv.company.domain;

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
@Table(name = "companies")
@Getter
@NoArgsConstructor
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 10)
	private String cik;

	@Column(nullable = false, length = 20)
	private String symbol;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(name = "financials_fetched_at")
	private Instant financialsFetchedAt;

	public Company(String cik, String symbol, String name) {
		this.cik = cik;
		this.symbol = symbol;
		this.name = name;
	}

	public void markFinancialsFetched(Instant fetchedAt) {
		financialsFetchedAt = fetchedAt;
	}
}

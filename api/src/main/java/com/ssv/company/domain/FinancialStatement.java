package com.ssv.company.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "financial_statements")
@Getter
@NoArgsConstructor
public class FinancialStatement {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(nullable = false, length = 255)
	private String metric;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal value;

	@Column(nullable = false, length = 20)
	private String unit;

	@Column(name = "period_end", length = 20)
	private String periodEnd;

	@Column(name = "fetched_at", nullable = false)
	private Instant fetchedAt;

	public FinancialStatement(FinancialStatementCreateRequest request) {
		company = request.company();
		metric = request.metric();
		value = request.value();
		unit = request.unit();
		periodEnd = request.periodEnd();
		fetchedAt = request.fetchedAt();
	}
}

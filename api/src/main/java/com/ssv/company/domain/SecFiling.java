package com.ssv.company.domain;

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
@Table(name = "sec_filings")
@Getter
@NoArgsConstructor
public class SecFiling {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "form_type", nullable = false, length = 20)
	private String formType;

	@Column(name = "filing_date", nullable = false, length = 20)
	private String filingDate;

	@Column(nullable = false, length = 500)
	private String url;

	@Column(name = "fetched_at", nullable = false)
	private Instant fetchedAt;

	public SecFiling(SecFilingCreateRequest request) {
		company = request.company();
		formType = request.formType();
		filingDate = request.filingDate();
		url = request.url();
		fetchedAt = request.fetchedAt();
	}
}

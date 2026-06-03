package com.ssv.company.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FinancialStatementTest {

	@Test
	void createsFinancialStatementFromRequest() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		Instant fetchedAt = Instant.parse("2026-05-31T10:00:00Z");

		FinancialStatementCreateRequest request = new FinancialStatementCreateRequest(company, "Revenues",
				BigDecimal.valueOf(1000), "USD", "2025-12-31", fetchedAt);

		FinancialStatement statement = new FinancialStatement(request);

		assertSame(company, statement.getCompany());
		assertEquals("Revenues", statement.getMetric());
		assertEquals(BigDecimal.valueOf(1000), statement.getValue());
		assertEquals("USD", statement.getUnit());
		assertEquals("2025-12-31", statement.getPeriodEnd());
		assertSame(fetchedAt, statement.getFetchedAt());
	}
}

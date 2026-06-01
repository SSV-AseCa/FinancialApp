package com.ssv.company.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SecFilingTest {

	@Test
	void createsSecFilingFromRequest() {
		Company company = new Company("0000320193", "AAPL", "Apple Inc.");
		Instant fetchedAt = Instant.parse("2026-05-31T10:00:00Z");

		SecFilingCreateRequest request = new SecFilingCreateRequest(company, "10-K", "2025-10-31",
				"https://www.sec.gov/example", fetchedAt);

		SecFiling filing = new SecFiling(request);

		assertSame(company, filing.getCompany());
		assertEquals("10-K", filing.getFormType());
		assertEquals("2025-10-31", filing.getFilingDate());
		assertEquals("https://www.sec.gov/example", filing.getUrl());
		assertSame(fetchedAt, filing.getFetchedAt());
	}
}

package com.ssv.company.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class CompanyTest {

    @Test
    void createsCompanyWithBasicData() {
        Company company = new Company("0000320193", "AAPL", "Apple Inc.");

        assertEquals("0000320193", company.getCik());
        assertEquals("AAPL", company.getSymbol());
        assertEquals("Apple Inc.", company.getName());
    }

    @Test
    void marksFinancialsFetchedAtGivenInstant() {
        Company company = new Company("0000320193", "AAPL", "Apple Inc.");
        Instant fetchedAt = Instant.parse("2026-05-31T10:00:00Z");

        company.markFinancialsFetched(fetchedAt);

        assertSame(fetchedAt, company.getFinancialsFetchedAt());
    }
}
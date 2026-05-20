package com.financialapp.company.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinancialStatementTest {

    @Test
    void shouldCreateFinancialStatement() {
        FinancialStatement statement = new FinancialStatement();

        assertNotNull(statement);
    }
}
package com.financialapp.company.persistence;

import com.financialapp.company.infrastructure.persistence.FinancialStatementRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinancialStatementRepositoryTest {

	@Test
	void shouldCreateFinancialStatementRepository() {
		FinancialStatementRepository repository = new FinancialStatementRepository();

		assertNotNull(repository);
	}
}

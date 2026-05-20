package com.financialapp.company.persistence;

import com.financialapp.company.infrastructure.persistence.CompanyRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompanyRepositoryTest {

	@Test
	void shouldCreateCompanyRepository() {
		CompanyRepository repository = new CompanyRepository();

		assertNotNull(repository);
	}
}

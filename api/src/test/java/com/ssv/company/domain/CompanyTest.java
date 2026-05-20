package com.ssv.company.domain;

import com.ssv.company.domain.Company;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompanyTest {

	@Test
	void shouldCreateCompany() {
		Company company = new Company();

		assertNotNull(company);
	}
}

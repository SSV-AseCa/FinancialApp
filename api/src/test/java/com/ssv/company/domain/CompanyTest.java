package com.ssv.company.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompanyTest {

	@Test
	void shouldCreateCompany() {
		Company company = new Company();

		assertNotNull(company);
	}
}

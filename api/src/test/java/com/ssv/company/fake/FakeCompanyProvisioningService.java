package com.ssv.company.fake;

import java.util.HashMap;
import java.util.Map;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.company.domain.Company;
import com.ssv.company.exceptions.CompanyNotFoundException;

/**
 * Registry-backed {@link CompanyProvisioningService} for tests. Only registered
 * CIKs resolve; an unknown CIK throws {@link CompanyNotFoundException},
 * mirroring EDGAR rejecting an identifier it does not recognize. Lets a test
 * assert both the resolved symbol and the validation behaviour without touching
 * EDGAR.
 */
public class FakeCompanyProvisioningService extends CompanyProvisioningService {

	private final Map<String, Company> companies = new HashMap<>();

	public FakeCompanyProvisioningService() {
		super(null, null, null, null);
	}

	public FakeCompanyProvisioningService register(String cik, String symbol, String name) {
		companies.put(cik, new Company(cik, symbol, name));
		return this;
	}

	@Override
	public Company ensureCompany(String cik) {
		Company company = companies.get(cik);
		if (company == null) {
			throw new CompanyNotFoundException(cik);
		}
		return company;
	}
}

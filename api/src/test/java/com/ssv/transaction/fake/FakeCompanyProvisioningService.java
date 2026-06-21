package com.ssv.transaction.fake;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.company.domain.Company;

/**
 * Resolves a CIK to a {@link Company} without touching EDGAR. The symbol equals
 * the CIK (identity), which keeps transaction-flow unit tests agnostic to the
 * symbol value.
 */
public class FakeCompanyProvisioningService extends CompanyProvisioningService {

	public FakeCompanyProvisioningService() {
		super(null, null, null, null);
	}

	@Override
	public Company ensureCompany(String cik) {
		return new Company(cik, cik, "Test Co");
	}
}

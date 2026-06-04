package com.ssv.company.application.fake;

import com.ssv.company.application.CompanyFinancialDataRefresher;
import com.ssv.company.domain.Company;

public class FakeCompanyFinancialDataRefresher extends CompanyFinancialDataRefresher {

	private boolean refreshResult;
	private Company lastRefreshed;

	public FakeCompanyFinancialDataRefresher() {
		super(null, null, null, null, null, null, null, null, null);
	}

	public void respondWith(boolean result) {
		this.refreshResult = result;
	}

	@Override
	public boolean refreshIfStale(Company company) {
		this.lastRefreshed = company;
		return refreshResult;
	}

	public Company lastRefreshed() {
		return lastRefreshed;
	}
}

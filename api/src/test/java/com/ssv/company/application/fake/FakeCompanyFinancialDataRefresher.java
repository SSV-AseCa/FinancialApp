package com.ssv.company.application.fake;

import com.ssv.company.application.CompanyFinancialDataRefresher;
import com.ssv.company.domain.Company;

public class FakeCompanyFinancialDataRefresher extends CompanyFinancialDataRefresher {

	private boolean refreshResult;
	private Company lastRefreshed;
	private RuntimeException failureToThrow;

	public FakeCompanyFinancialDataRefresher() {
		super(null, null, null, null, null, null, null, null);
	}

	public void respondWith(boolean result) {
		this.refreshResult = result;
	}

	public void failWith(RuntimeException exception) {
		this.failureToThrow = exception;
	}

	@Override
	public boolean refreshIfStale(Company company) {
		this.lastRefreshed = company;
		if (failureToThrow != null) {
			throw failureToThrow;
		}
		return refreshResult;
	}

	public Company lastRefreshed() {
		return lastRefreshed;
	}
}

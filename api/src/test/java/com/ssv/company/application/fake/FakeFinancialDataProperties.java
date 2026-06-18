package com.ssv.company.application.fake;

import com.ssv.company.application.FinancialDataProperties;

public class FakeFinancialDataProperties implements FinancialDataProperties {

	@Override
	public String submissionsPath() {
		return "/submissions/CIK%s.json";
	}

	@Override
	public String companyFactsPath() {
		return "/api/xbrl/companyfacts/CIK%s.json";
	}

	@Override
	public int stalenessDays() {
		return 1;
	}
}

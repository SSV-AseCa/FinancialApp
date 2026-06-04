package com.ssv.company.application.fake;

import java.util.List;

import com.ssv.company.application.EdgarFinancialMetric;
import com.ssv.edgar.application.EdgarCompanyFactsParser;

public class FakeEdgarCompanyFactsParser extends EdgarCompanyFactsParser {

	public FakeEdgarCompanyFactsParser() {
		super(null);
	}

	@Override
	public List<EdgarFinancialMetric> parse(String payload) {
		return List.of();
	}
}

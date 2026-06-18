package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;

public class FakeCompanyMetricsService extends CompanyMetricsService {

	private final Map<String, List<FinancialMetricResponse>> responses = new HashMap<>();
	private final List<String> notFoundCiks = new ArrayList<>();

	public FakeCompanyMetricsService() {
		super(null, null, null, null);
	}

	public void respondWith(String cik, List<FinancialMetricResponse> metrics) {
		responses.put(cik, metrics);
	}

	public void notFoundFor(String cik) {
		notFoundCiks.add(cik);
	}

	public void reset() {
		responses.clear();
		notFoundCiks.clear();
	}

	@Override
	public List<FinancialMetricResponse> getMetrics(String cik) {
		if (notFoundCiks.contains(cik)) {
			throw new CompanyNotFoundException(cik);
		}
		return responses.getOrDefault(cik, List.of());
	}
}

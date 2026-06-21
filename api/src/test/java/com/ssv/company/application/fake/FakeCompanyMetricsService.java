package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.ssv.company.application.CompanyMetricsService;
import com.ssv.company.dto.FinancialMetricResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.shared.dto.PageResponse;

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
	public PageResponse<FinancialMetricResponse> getMetrics(String cik, String query, Pageable pageable) {
		if (notFoundCiks.contains(cik)) {
			throw new CompanyNotFoundException(cik);
		}
		List<FinancialMetricResponse> all = responses.getOrDefault(cik, List.of());
		List<FinancialMetricResponse> filtered = (query == null || query.isBlank())
				? all
				: all.stream().filter(metric -> metric.metric().toLowerCase(Locale.ROOT)
						.contains(query.strip().toLowerCase(Locale.ROOT))).toList();
		return FakePages.of(filtered, pageable);
	}
}

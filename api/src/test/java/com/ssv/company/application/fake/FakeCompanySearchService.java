package com.ssv.company.application.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssv.company.application.CompanySearchService;
import com.ssv.company.dto.CompanySearchResult;

public class FakeCompanySearchService extends CompanySearchService {

	private final Map<String, List<CompanySearchResult>> responses = new HashMap<>();

	public FakeCompanySearchService() {
		super(null, null, null);
	}

	public void respondWith(String query, List<CompanySearchResult> results) {
		responses.put(query, results);
	}

	public void reset() {
		responses.clear();
	}

	@Override
	public List<CompanySearchResult> searchCompanies(String query) {
		return responses.getOrDefault(query, List.of());
	}
}

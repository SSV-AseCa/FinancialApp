package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssv.company.application.CompanyFilingsService;
import com.ssv.company.dto.SecFilingResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;

public class FakeCompanyFilingsService extends CompanyFilingsService {

	private final Map<String, List<SecFilingResponse>> responses = new HashMap<>();
	private final List<String> notFoundCiks = new ArrayList<>();

	public FakeCompanyFilingsService() {
		super(null, null, null);
	}

	public void respondWith(String cik, List<SecFilingResponse> filings) {
		responses.put(cik, filings);
	}

	public void notFoundFor(String cik) {
		notFoundCiks.add(cik);
	}

	public void reset() {
		responses.clear();
		notFoundCiks.clear();
	}

	@Override
	public List<SecFilingResponse> getFilings(String cik) {
		if (notFoundCiks.contains(cik)) {
			throw new CompanyNotFoundException(cik);
		}
		return responses.getOrDefault(cik, List.of());
	}
}

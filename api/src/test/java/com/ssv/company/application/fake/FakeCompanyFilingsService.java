package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.ssv.company.application.CompanyFilingsService;
import com.ssv.company.dto.SecFilingResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.shared.dto.PageResponse;

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
	public PageResponse<SecFilingResponse> getFilings(String cik, String query, Pageable pageable) {
		if (notFoundCiks.contains(cik)) {
			throw new CompanyNotFoundException(cik);
		}
		List<SecFilingResponse> all = responses.getOrDefault(cik, List.of());
		List<SecFilingResponse> filtered = (query == null || query.isBlank())
				? all
				: all.stream().filter(filing -> matches(filing, query.strip().toLowerCase(Locale.ROOT))).toList();
		return FakePages.of(filtered, pageable);
	}

	private static boolean matches(SecFilingResponse filing, String needle) {
		String formType = filing.formType() == null ? "" : filing.formType().toLowerCase(Locale.ROOT);
		String description = filing.description() == null ? "" : filing.description().toLowerCase(Locale.ROOT);
		return formType.contains(needle) || description.contains(needle);
	}
}

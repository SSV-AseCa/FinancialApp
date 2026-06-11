package com.ssv.company.application;

import com.ssv.company.dto.CompanyFilingResponse;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.edgar.application.EdgarCompanyFilingsParser;
import com.ssv.edgar.application.EdgarClient;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyFilingsService {

	private final CompanyStore companyStore;
	private final FinancialDataProperties properties;
	private final EdgarClient edgarClient;
	private final EdgarCompanyFilingsParser filingsParser;

	private static final Comparator<CompanyFilingResponse> BY_DATE_DESC = Comparator
			.comparing((CompanyFilingResponse r) -> LocalDate.parse(r.filingDate())).reversed();

	public List<CompanyFilingResponse> getFilings(String cik, int limit) {
		int safeLimit = sanitizeLimit(limit);
		String normalized = normalizeCik(cik);
		ensureCompanyExists(normalized);
		return fetchFilings(normalized).stream().sorted(BY_DATE_DESC).limit(safeLimit).collect(Collectors.toList());
	}

	private void ensureCompanyExists(String cik) {
		companyStore.findByCik(cik).orElseThrow(() -> new CompanyNotFoundException(cik));
	}

	private List<CompanyFilingResponse> fetchFilings(String cik) {
		String path = properties.submissionsPath().formatted(cik);
		String payload = edgarClient.get(path);
		return filingsParser.parse(payload).stream()
				.map(f -> new CompanyFilingResponse(f.formType(), f.filingDate(), f.url()))
				.collect(Collectors.toList());
	}

	private int sanitizeLimit(int limit) {
		final int DEFAULT = 10;
		final int MAX = 100;
		if (limit <= 0) {
			return DEFAULT;
		}
		return Math.min(limit, MAX);
	}

	private String normalizeCik(String cik) {
		return "%010d".formatted(Long.parseLong(cik.strip()));
	}
}

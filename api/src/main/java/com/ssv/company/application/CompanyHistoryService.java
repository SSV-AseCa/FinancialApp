package com.ssv.company.application;

import com.ssv.company.dto.CompanyHistoryPoint;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.edgar.application.EdgarClient;
import java.util.List;

import com.ssv.edgar.application.EdgarCompanyFactsParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyHistoryService {

	private static final String CIK_FORMAT = "%010d";

	private final CompanyStore companyStore;
	private final EdgarClient edgarClient;
	private final EdgarCompanyFactsParser factsParser;
	private final FinancialDataProperties properties;
	private final CompanyHistoryMapper mapper;

	public List<CompanyHistoryPoint> historyByCik(String cik) {
		String normalized = normalizeCik(cik);
		ensureCompanyExists(normalized, cik);
		return mapper.toHistoryPoints(parseMetrics(normalized));
	}

	private void ensureCompanyExists(String normalized, String original) {
		companyStore.findByCik(normalized).orElseThrow(() -> new CompanyNotFoundException(original));
	}

	private List<EdgarFinancialMetric> parseMetrics(String normalized) {
		String path = properties.companyFactsPath().formatted(normalized);
		return factsParser.parse(edgarClient.get(path));
	}

	private String normalizeCik(String cik) {
		return CIK_FORMAT.formatted(Long.parseLong(cik.strip()));
	}
}

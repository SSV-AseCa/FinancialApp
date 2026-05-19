package com.financialapp.company.application;

import com.financialapp.edgar.application.EdgarClient;
import com.financialapp.edgar.infrastructure.config.EdgarProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyResearchService {

	private final EdgarClient edgarClient;
	private final EdgarProperties properties;

	public String fetchCompanySubmissions(String cik) {
		String normalizedCik = cik.strip();
		String path = properties.submissionsPath().formatted(normalizedCik);
		return edgarClient.get(path);
	}
}

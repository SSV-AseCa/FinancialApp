package com.ssv.company.application;

import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.config.EdgarProperties;
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

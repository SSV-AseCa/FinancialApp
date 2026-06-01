package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.company.infrastructure.persistence.CompanyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyResearchService {

	private final CompanyRepository companyRepository;
	private final CompanyFinancialDataRefresher refresher;

	@Transactional
	public CompanyFinancialData getOrFetchFinancialData(CompanyRequest request) {
		Company company = findOrCreate(request);
		boolean refreshed = refresher.refreshIfStale(company);
		return new CompanyFinancialData(company, refreshed);
	}

	private Company findOrCreate(CompanyRequest request) {
		String cik = normalizeCik(request.cik());
		return companyRepository.findByCik(cik).orElseGet(() -> saveCompany(request, cik));
	}

	private Company saveCompany(CompanyRequest request, String cik) {
		return companyRepository.save(new Company(cik, request.symbol().strip(), request.name().strip()));
	}

	private String normalizeCik(String cik) {
		return "%010d".formatted(Long.parseLong(cik.strip()));
	}
}

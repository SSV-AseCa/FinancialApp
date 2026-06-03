package com.ssv.company.application;

import com.ssv.company.domain.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyResearchService {

	private final CompanyStore companyStore;
	private final CompanyFinancialDataRefresher refresher;

	@Transactional
	public CompanyFinancialData getOrFetchFinancialData(CompanyRequest request) {
		Company company = findOrCreate(request);
		boolean refreshed = refresher.refreshIfStale(company);
		persistIfRefreshed(company, refreshed);
		return new CompanyFinancialData(company, refreshed);
	}

	private void persistIfRefreshed(Company company, boolean refreshed) {
		if (refreshed) {
			companyStore.save(company);
		}
	}

	private Company findOrCreate(CompanyRequest request) {
		String cik = normalizeCik(request.cik());
		return companyStore.findByCik(cik).orElseGet(() -> saveCompany(request, cik));
	}

	private Company saveCompany(CompanyRequest request, String cik) {
		try {
			return companyStore.save(new Company(cik, request.symbol().strip(), request.name().strip()));
		} catch (DataIntegrityViolationException exception) {
			return findExistingAfterConflict(cik, exception);
		}
	}

	private Company findExistingAfterConflict(String cik, RuntimeException exception) {
		return companyStore.findByCik(cik).orElseThrow(() -> exception);
	}

	private String normalizeCik(String cik) {
		return "%010d".formatted(Long.parseLong(cik.strip()));
	}
}

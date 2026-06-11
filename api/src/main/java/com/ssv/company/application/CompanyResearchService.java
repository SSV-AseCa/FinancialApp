package com.ssv.company.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssv.company.domain.CikUtils;
import com.ssv.company.domain.Company;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyResearchService {

	private final CompanyStore companyStore;
	private final CompanyFinancialDataRefresher refresher;

	@Transactional
	public CompanyFinancialData getOrFetchFinancialData(CompanyRequest request) {
		Company company = findOrCreate(request);
		boolean refreshed = refresher.refreshIfStale(company);
		if (refreshed) {
			companyStore.save(company);
		}
		return new CompanyFinancialData(company, refreshed);
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
		return CikUtils.normalize(cik);
	}
}

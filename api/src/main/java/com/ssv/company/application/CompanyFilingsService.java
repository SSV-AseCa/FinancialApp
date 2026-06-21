package com.ssv.company.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.SecFiling;
import com.ssv.company.dto.SecFilingResponse;
import com.ssv.company.infrastructure.persistence.SecFilingRepository;
import com.ssv.shared.dto.PageResponse;
import com.ssv.shared.exceptions.EdgarUnavailableException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyFilingsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyFilingsService.class);

	private final CompanyProvisioningService companyProvisioningService;
	private final CompanyFinancialDataRefresher refresher;
	private final SecFilingRepository secFilingRepository;

	public PageResponse<SecFilingResponse> getFilings(String cik, String query, Pageable pageable) {
		Company company = companyProvisioningService.ensureCompany(cik);
		refreshQuietly(company);
		Page<SecFiling> page = (query == null || query.isBlank())
				? secFilingRepository.findByCompanyId(company.getId(), pageable)
				: secFilingRepository.searchByCompanyId(company.getId(), query.strip(), pageable);
		return PageResponse.of(page.map(this::toResponse));
	}

	/**
	 * Refreshes from EDGAR but tolerates an unavailable provider: a known company
	 * keeps serving whatever filings are already persisted rather than failing the
	 * request when EDGAR is down or rate-limiting.
	 */
	private void refreshQuietly(Company company) {
		try {
			refresher.refreshIfStale(company);
		} catch (EdgarUnavailableException exception) {
			LOGGER.warn("EDGAR refresh failed for CIK {} — serving cached filings", company.getCik(), exception);
		}
	}

	private SecFilingResponse toResponse(SecFiling filing) {
		return new SecFilingResponse(filing.getFormType(), filing.getFilingDate(), filing.getDescription());
	}
}

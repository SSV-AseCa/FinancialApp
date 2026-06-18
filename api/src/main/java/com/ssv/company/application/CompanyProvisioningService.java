package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.application.EdgarCompanyProfileParser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Ensures a {@link Company} exists locally for a given CIK. When the company is
 * already cached it is returned untouched; otherwise its identity is fetched
 * from EDGAR and persisted, so callers can treat any valid CIK as known without
 * a prior search. A CIK EDGAR does not recognize surfaces as
 * {@link CompanyNotFoundException}.
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyProvisioningService {

	private static final String CIK_FORMAT = "%010d";

	private final CompanyStore companyStore;
	private final EdgarClient edgarClient;
	private final EdgarCompanyProfileParser profileParser;
	private final FinancialDataProperties properties;

	public Company ensureCompany(String cik) {
		String normalized = normalizeCik(cik);
		return companyStore.findByCik(normalized).orElseGet(() -> fetchAndSave(normalized));
	}

	private Company fetchAndSave(String cik) {
		EdgarCompanyProfile profile = profileParser.parse(fetchSubmissions(cik))
				.orElseThrow(() -> new CompanyNotFoundException(cik));
		return save(cik, profile);
	}

	private String fetchSubmissions(String cik) {
		return edgarClient.get(properties.submissionsPath().formatted(cik));
	}

	private Company save(String cik, EdgarCompanyProfile profile) {
		try {
			return companyStore.save(new Company(cik, profile.symbol().strip(), profile.name().strip()));
		} catch (DataIntegrityViolationException exception) {
			return companyStore.findByCik(cik).orElseThrow(() -> new CompanyNotFoundException(cik));
		}
	}

	private String normalizeCik(String cik) {
		try {
			return CIK_FORMAT.formatted(Long.parseLong(cik.strip()));
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid CIK");
		}
	}
}

package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.config.EdgarProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyFinancialDataRefresher {

	private final EdgarProperties properties;
	private final EdgarClient edgarClient;
	private final Clock clock;

	public boolean refreshIfStale(Company company) {
		if (isFresh(company)) {
			return false;
		}
		company.markFinancialsFetched(Instant.now(clock));
		return true;
	}

	public String fetchCompanySubmissions(String cik) {
		String normalizedCik = cik.strip();
		String path = properties.submissionsPath().formatted(normalizedCik);
		return edgarClient.get(path);
	}

	private boolean isFresh(Company company) {
		Instant fetchedAt = company.getFinancialsFetchedAt();
		return fetchedAt != null && fetchedAt.isAfter(stalenessCutoff());
	}

	private Instant stalenessCutoff() {
		return Instant.now(clock).minus(properties.stalenessDays(), ChronoUnit.DAYS);
	}
}

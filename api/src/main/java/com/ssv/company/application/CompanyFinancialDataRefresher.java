package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.SecFiling;
import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.application.EdgarCompanyFactsParser;
import com.ssv.edgar.application.EdgarCompanyFilingsParser;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyFinancialDataRefresher {

	private final FinancialDataProperties properties;
	private final EdgarClient edgarClient;
	private final EdgarCompanyFactsParser factsParser;
	private final EdgarCompanyFilingsParser filingsParser;
	private final FinancialStatementStore financialStatementStore;
	private final SecFilingStore secFilingStore;
	private final FinancialStatementFactory financialStatementFactory;
	private final SecFilingFactory secFilingFactory;
	private final Clock clock;

	public boolean refreshIfStale(Company company) {
		if (isFresh(company)) {
			return false;
		}
		refreshFinancialData(company);
		return true;
	}

	public String fetchCompanySubmissions(String cik) {
		String normalizedCik = cik.strip();
		String path = properties.submissionsPath().formatted(normalizedCik);
		return edgarClient.get(path);
	}

	private void refreshFinancialData(Company company) {
		Instant fetchedAt = Instant.now(clock);
		List<FinancialStatement> statements = financialStatements(company, fetchedAt);
		List<SecFiling> filings = secFilings(company, fetchedAt);
		replaceFinancialData(company, statements, filings);
		company.markFinancialsFetched(fetchedAt);
	}

	private List<FinancialStatement> financialStatements(Company company, Instant fetchedAt) {
		return factsParser.parse(fetchCompanyFacts(company.getCik())).stream()
				.map(metric -> financialStatementFactory.create(company, metric, fetchedAt)).toList();
	}

	private List<SecFiling> secFilings(Company company, Instant fetchedAt) {
		return filingsParser.parse(fetchCompanySubmissions(company.getCik())).stream()
				.map(filing -> secFilingFactory.create(company, filing, fetchedAt)).toList();
	}

	private String fetchCompanyFacts(String cik) {
		String normalizedCik = cik.strip();
		String path = properties.companyFactsPath().formatted(normalizedCik);
		return edgarClient.get(path);
	}

	private void replaceFinancialData(Company company, List<FinancialStatement> statements, List<SecFiling> filings) {
		financialStatementStore.deleteByCompanyId(company.getId());
		secFilingStore.deleteByCompanyId(company.getId());
		financialStatementStore.saveAll(statements);
		secFilingStore.saveAll(filings);
	}

	private boolean isFresh(Company company) {
		Instant fetchedAt = company.getFinancialsFetchedAt();
		return fetchedAt != null && fetchedAt.isAfter(stalenessCutoff());
	}

	private Instant stalenessCutoff() {
		return Instant.now(clock).minus(properties.stalenessDays(), ChronoUnit.DAYS);
	}
}
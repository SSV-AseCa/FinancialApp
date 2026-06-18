package com.ssv.company.application.fake;

import java.time.Instant;
import java.util.List;

import com.ssv.company.application.FinancialDataPersister;
import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.SecFiling;

public class FakeFinancialDataPersister extends FinancialDataPersister {

	private int replaceCallCount;
	private Company lastCompany;
	private List<FinancialStatement> lastStatements;
	private List<SecFiling> lastFilings;
	private Instant lastFetchedAt;

	public FakeFinancialDataPersister() {
		super(null, null, null);
	}

	@Override
	public void replace(Company company, List<FinancialStatement> statements, List<SecFiling> filings,
			Instant fetchedAt) {
		replaceCallCount++;
		lastCompany = company;
		lastStatements = statements;
		lastFilings = filings;
		lastFetchedAt = fetchedAt;
	}

	public boolean wasReplaceCalled() {
		return replaceCallCount > 0;
	}

	public Company lastCompany() {
		return lastCompany;
	}

	public List<FinancialStatement> lastStatements() {
		return lastStatements;
	}

	public List<SecFiling> lastFilings() {
		return lastFilings;
	}

	public Instant lastFetchedAt() {
		return lastFetchedAt;
	}
}

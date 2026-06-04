package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ssv.company.application.FinancialStatementStore;
import com.ssv.company.domain.FinancialStatement;

public class FakeFinancialStatementStore implements FinancialStatementStore {

	private int deleteCallCount;
	private final List<FinancialStatement> saved = new ArrayList<>();

	@Override
	public void deleteByCompanyId(UUID companyId) {
		deleteCallCount++;
	}

	@Override
	public <S extends FinancialStatement> Iterable<S> saveAll(Iterable<S> statements) {
		statements.forEach(saved::add);
		return statements;
	}

	public boolean wasDeleteCalled() {
		return deleteCallCount > 0;
	}

	public int deleteCallCount() {
		return deleteCallCount;
	}

	public List<FinancialStatement> saved() {
		return saved;
	}
}

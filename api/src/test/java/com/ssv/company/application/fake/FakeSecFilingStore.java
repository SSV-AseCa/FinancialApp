package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ssv.company.application.SecFilingStore;
import com.ssv.company.domain.SecFiling;

public class FakeSecFilingStore implements SecFilingStore {

	private int deleteCallCount;
	private final List<SecFiling> saved = new ArrayList<>();

	@Override
	public void deleteByCompanyId(UUID companyId) {
		deleteCallCount++;
	}

	@Override
	public <S extends SecFiling> Iterable<S> saveAll(Iterable<S> filings) {
		filings.forEach(saved::add);
		return filings;
	}

	public boolean wasDeleteCalled() {
		return deleteCallCount > 0;
	}

	public int deleteCallCount() {
		return deleteCallCount;
	}

	public List<SecFiling> saved() {
		return saved;
	}
}

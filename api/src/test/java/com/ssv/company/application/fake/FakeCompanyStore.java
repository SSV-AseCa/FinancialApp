package com.ssv.company.application.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeCompanyStore implements CompanyStore {

	private final Map<String, Company> byCik = new HashMap<>();
	private RuntimeException saveException;
	private boolean firstFindReturnsEmpty;
	private int findCallCount;

	public void seed(Company c) {
		byCik.put(c.getCik(), c);
	}

	public void throwOnNextSave(RuntimeException ex) {
		this.saveException = ex;
	}

	public void firstFindReturnsEmpty() {
		this.firstFindReturnsEmpty = true;
	}

	@Override
	public Optional<Company> findByCik(String cik) {
		findCallCount++;
		if (firstFindReturnsEmpty && findCallCount == 1) {
			return Optional.empty();
		}
		return Optional.ofNullable(byCik.get(cik));
	}

	@Override
	public Optional<Company> findById(UUID id) {
		return byCik.values().stream().filter(c -> id.equals(c.getId())).findFirst();
	}

	@Override
	public Company save(Company c) {
		if (saveException != null) {
			RuntimeException ex = saveException;
			saveException = null;
			throw ex;
		}
		byCik.put(c.getCik(), c);
		return c;
	}
}

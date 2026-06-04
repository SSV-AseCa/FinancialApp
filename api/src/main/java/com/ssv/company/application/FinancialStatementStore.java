package com.ssv.company.application;

import com.ssv.company.domain.FinancialStatement;
import java.util.List;
import java.util.UUID;

public interface FinancialStatementStore {

	void deleteByCompanyId(UUID companyId);

	List<FinancialStatement> findByCompanyId(UUID companyId);

	<S extends FinancialStatement> Iterable<S> saveAll(Iterable<S> statements);
}

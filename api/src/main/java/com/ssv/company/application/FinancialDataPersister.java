package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.SecFiling;
import java.time.Instant;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Atomically replaces a company's persisted financial data within a single
 * transaction. The transaction is required: the derived
 * {@code deleteByCompanyId} runs as per-entity removes, which the JPA
 * EntityManager refuses outside a transaction. Keeping delete, insert and the
 * freshness stamp together also guarantees the data is never left
 * half-replaced.
 *
 * EDGAR I/O is performed by the caller beforehand, so no database connection is
 * held while waiting on the network.
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class FinancialDataPersister {

	private final FinancialStatementStore financialStatementStore;
	private final SecFilingStore secFilingStore;
	private final CompanyStore companyStore;

	@Transactional
	public void replace(Company company, List<FinancialStatement> statements, List<SecFiling> filings,
			Instant fetchedAt) {
		financialStatementStore.deleteByCompanyId(company.getId());
		secFilingStore.deleteByCompanyId(company.getId());
		financialStatementStore.saveAll(statements);
		secFilingStore.saveAll(filings);
		company.markFinancialsFetched(fetchedAt);
		companyStore.save(company);
	}
}

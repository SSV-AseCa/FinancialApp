package com.ssv.company.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import com.ssv.company.application.FinancialStatementStore;
import com.ssv.company.domain.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialStatementRepository extends FinancialStatementStore, JpaRepository<FinancialStatement, UUID> {
	void deleteByCompanyId(UUID companyId);

	List<FinancialStatement> findByCompanyId(UUID companyId);
}

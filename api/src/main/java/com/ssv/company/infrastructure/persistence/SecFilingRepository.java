package com.ssv.company.infrastructure.persistence;

import java.util.UUID;

import com.ssv.company.application.SecFilingStore;
import com.ssv.company.domain.SecFiling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecFilingRepository extends SecFilingStore, JpaRepository<SecFiling, UUID> {
	void deleteByCompanyId(UUID companyId);
}

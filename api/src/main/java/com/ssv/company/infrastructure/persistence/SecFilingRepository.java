package com.ssv.company.infrastructure.persistence;

import java.util.UUID;

import com.ssv.company.domain.SecFiling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecFilingRepository extends JpaRepository<SecFiling, UUID> {
	void deleteByCompanyId(UUID companyId);
}

package com.ssv.company.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import com.ssv.company.application.SecFilingStore;
import com.ssv.company.domain.SecFiling;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SecFilingRepository extends SecFilingStore, JpaRepository<SecFiling, UUID> {
	void deleteByCompanyId(UUID companyId);

	List<SecFiling> findByCompanyId(UUID companyId);

	Page<SecFiling> findByCompanyId(UUID companyId, Pageable pageable);

	@Query("select f from SecFiling f where f.company.id = :companyId "
			+ "and (lower(f.formType) like lower(concat('%', :q, '%')) "
			+ "or lower(f.description) like lower(concat('%', :q, '%')))")
	Page<SecFiling> searchByCompanyId(@Param("companyId") UUID companyId, @Param("q") String q, Pageable pageable);
}

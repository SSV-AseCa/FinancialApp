package com.ssv.company.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import com.ssv.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
	Optional<Company> findByCik(String cik);
}

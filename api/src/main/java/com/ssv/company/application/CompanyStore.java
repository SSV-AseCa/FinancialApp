package com.ssv.company.application;

import com.ssv.company.domain.Company;
import java.util.Optional;
import java.util.UUID;

public interface CompanyStore {

	Optional<Company> findByCik(String cik);

	Optional<Company> findById(UUID id);

	Company save(Company company);
}

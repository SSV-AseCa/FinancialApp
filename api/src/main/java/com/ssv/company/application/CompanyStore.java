package com.ssv.company.application;

import com.ssv.company.domain.Company;
import java.util.Optional;

public interface CompanyStore {

	Optional<Company> findByCik(String cik);

	Company save(Company company);
}

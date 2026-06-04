package com.ssv.company.application;

import com.ssv.company.domain.SecFiling;
import java.util.List;
import java.util.UUID;

public interface SecFilingStore {

	void deleteByCompanyId(UUID companyId);

	List<SecFiling> findByCompanyId(UUID companyId);

	<S extends SecFiling> Iterable<S> saveAll(Iterable<S> filings);
}

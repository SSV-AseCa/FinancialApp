package com.ssv.company.application;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.SecFiling;
import com.ssv.company.domain.SecFilingCreateRequest;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SecFilingFactory {

	public SecFiling create(Company company, EdgarSecFiling filing, Instant fetchedAt) {
		SecFilingCreateRequest request = new SecFilingCreateRequest(company, filing.formType(), filing.filingDate(),
				filing.url(), filing.description(), fetchedAt);
		return new SecFiling(request);
	}
}

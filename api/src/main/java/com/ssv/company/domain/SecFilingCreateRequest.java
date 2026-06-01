package com.ssv.company.domain;

import java.time.Instant;

public record SecFilingCreateRequest(Company company, String formType, String filingDate, String url,
		Instant fetchedAt) {
}

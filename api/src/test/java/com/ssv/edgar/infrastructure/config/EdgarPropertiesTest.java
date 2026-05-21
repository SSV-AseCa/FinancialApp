package com.ssv.edgar.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgarPropertiesTest {

	@Test
	void shouldCreateEdgarProperties() {
		EdgarProperties.RateLimit rateLimit = new EdgarProperties.RateLimit(10, 1000L);

		EdgarProperties properties = new EdgarProperties("https://www.sec.gov", "FinancialApp", "/submissions/%s.json",
				rateLimit);

		assertEquals("https://www.sec.gov", properties.baseUrl());
		assertEquals("FinancialApp", properties.userAgent());
		assertEquals("/submissions/%s.json", properties.submissionsPath());
		assertEquals(rateLimit, properties.rateLimit());
	}

	@Test
	void shouldCreateRateLimit() {
		EdgarProperties.RateLimit rateLimit = new EdgarProperties.RateLimit(5, 2000L);

		assertEquals(5, rateLimit.maxRequests());
		assertEquals(2000L, rateLimit.windowMillis());
	}
}

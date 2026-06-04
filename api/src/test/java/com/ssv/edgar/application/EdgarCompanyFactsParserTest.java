package com.ssv.edgar.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.EdgarFinancialMetric;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class EdgarCompanyFactsParserTest {

	@Test
	void parseReturnsFinancialMetricsFromUsGaapFacts() {
		String payload = """
				{"facts":{"us-gaap":{"Assets":{"units":{"USD":[{"val":123.45,"end":"2025-12-31"}]}}}}}
				""";

		List<EdgarFinancialMetric> metrics = new EdgarCompanyFactsParser(new ObjectMapper()).parse(payload);

		assertEquals(1, metrics.size());
		assertEquals("Assets", metrics.getFirst().metric());
		assertEquals(new BigDecimal("123.45"), metrics.getFirst().value());
		assertEquals("USD", metrics.getFirst().unit());
		assertEquals("2025-12-31", metrics.getFirst().periodEnd());
	}
}

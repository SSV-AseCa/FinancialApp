package com.ssv.edgar.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.EdgarSecFiling;
import java.util.List;
import org.junit.jupiter.api.Test;

class EdgarCompanyFilingsParserTest {

	@Test
	void parseReturnsRecentFilings() {
		String payload = """
				{"filings":{"recent":{"form":["10-K"],"filingDate":["2025-12-31"],
				"primaryDocument":["aapl-20251231.htm"],"primaryDocDescription":["Annual report"]}}}
				""";

		List<EdgarSecFiling> filings = new EdgarCompanyFilingsParser(new ObjectMapper()).parse(payload);

		assertEquals(1, filings.size());
		assertEquals("10-K", filings.getFirst().formType());
		assertEquals("2025-12-31", filings.getFirst().filingDate());
		assertEquals("aapl-20251231.htm", filings.getFirst().url());
		assertEquals("Annual report", filings.getFirst().description());
	}
}

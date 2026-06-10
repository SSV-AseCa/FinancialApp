package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.fake.FakeEdgarClient;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.company.exceptions.CompanySearchParseException;
import com.ssv.edgar.infrastructure.config.EdgarProperties;

class CompanySearchServiceTest {

	private static final EdgarProperties PROPS = new EdgarProperties("https://data.sec.gov", "test-agent", "",
			"/submissions/CIK%s.json", "/api/xbrl/companyfacts/CIK%s.json", 1, "https://efts.sec.gov",
			"/LATEST/search-index", new EdgarProperties.RateLimit(10, 1000));

	private FakeEdgarClient searchClient;
	private CompanySearchService service;

	@BeforeEach
	void setUp() {
		searchClient = new FakeEdgarClient("");
		service = new CompanySearchService(searchClient, PROPS, new ObjectMapper());
	}

	@Test
	void buildsCorrectSearchPathWithQuery() {
		searchClient.setResponse(edgarResponse("Apple Inc.", "0000320193"));
		service.searchCompanies("apple");
		assertEquals("/LATEST/search-index?q=apple", searchClient.receivedPath());
	}

	@Test
	void parsesNameAndCikFromDisplayName() {
		searchClient.setResponse(edgarResponse("Apple Inc.", "0000320193"));
		List<CompanySearchResult> results = service.searchCompanies("apple");
		assertEquals(1, results.size());
		assertEquals("Apple Inc.", results.get(0).name());
		assertEquals("0000320193", results.get(0).cik());
	}

	@Test
	void extractsTickerFromDisplayName() {
		searchClient.setResponse(edgarResponseWithTicker("APPLE COMPUTER INC", "AAPL", "0000320193"));
		List<CompanySearchResult> results = service.searchCompanies("apple");
		assertEquals(1, results.size());
		assertEquals("APPLE COMPUTER INC", results.get(0).name());
		assertEquals(List.of("AAPL"), results.get(0).tickers());
	}

	@Test
	void deduplicatesByCik() {
		searchClient.setResponse(edgarResponseMultiple());
		List<CompanySearchResult> results = service.searchCompanies("apple");
		assertEquals(1, results.size());
		assertEquals("Apple Inc.", results.get(0).name());
	}

	@Test
	void returnsEmptyListWhenNoHits() {
		searchClient.setResponse("{\"hits\":{\"hits\":[]}}");
		List<CompanySearchResult> results = service.searchCompanies("unknownxyz");
		assertTrue(results.isEmpty());
	}

	@Test
	void throwsCompanySearchParseExceptionOnMalformedJson() {
		searchClient.setResponse("not-valid-json");
		assertThrows(CompanySearchParseException.class, () -> service.searchCompanies("apple"));
	}

	private static String edgarResponse(String name, String cik) {
		return singleHit(name + "  (CIK " + cik + ")", cik);
	}

	private static String edgarResponseWithTicker(String name, String ticker, String cik) {
		return singleHit(name + "  (" + ticker + ")  (CIK " + cik + ")", cik);
	}

	private static String edgarResponseMultiple() {
		String hit = sourceHit("Apple Inc.  (CIK 0000320193)", "0000320193");
		return "{\"hits\":{\"hits\":[" + hit + "," + hit + "]}}";
	}

	private static String singleHit(String displayName, String cik) {
		return "{\"hits\":{\"hits\":[" + sourceHit(displayName, cik) + "]}}";
	}

	private static String sourceHit(String displayName, String cik) {
		return "{\"_source\":{\"ciks\":[\"" + cik + "\"],\"display_names\":[\"" + displayName + "\"]}}";
	}
}

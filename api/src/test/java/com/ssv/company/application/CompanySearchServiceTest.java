package com.ssv.company.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.EdgarClient.FakeEdgarClient;
import com.ssv.company.dto.CompanySearchResult;
import com.ssv.edgar.infrastructure.config.EdgarProperties;

class CompanySearchServiceTest {

	private static final EdgarProperties PROPS = new EdgarProperties("https://data.sec.gov", "test-agent",
			"/submissions/CIK%s.json", "https://efts.sec.gov", "/LATEST/search-index",
			new EdgarProperties.RateLimit(10, 1000));

	private FakeEdgarClient searchClient;
	private CompanyResearchService service;

	@BeforeEach
	void setUp() {
		searchClient = new FakeEdgarClient("");
		service = new CompanyResearchService(new FakeEdgarClient(""), searchClient, PROPS, new ObjectMapper());
	}

	@Test
	void buildsCorrectSearchPathWithQuery() {
		searchClient.setResponse(edgarResponse("Apple Inc.", "0000320193"));
		service.searchCompanies("apple");
		assertEquals("/LATEST/search-index?q=apple", searchClient.receivedPath());
	}

	@Test
	void parsesEntityNameAndCikFromResponse() {
		searchClient.setResponse(edgarResponse("Apple Inc.", "0000320193"));
		List<CompanySearchResult> results = service.searchCompanies("apple");
		assertEquals(1, results.size());
		assertEquals("Apple Inc.", results.get(0).name());
		assertEquals("0000320193", results.get(0).cik());
	}

	@Test
	void deduplicatesByEntityId() {
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
	void throwsEdgarParseExceptionOnMalformedJson() {
		searchClient.setResponse("not-valid-json");
		assertThrows(EdgarParseException.class, () -> service.searchCompanies("apple"));
	}

	private static String edgarResponse(String name, String cik) {
		return "{\"hits\":{\"hits\":[{\"_source\":{\"entity_name\":\"" + name + "\",\"entity_id\":\"" + cik + "\"}}]}}";
	}

	private static String edgarResponseMultiple() {
		return "{\"hits\":{\"hits\":[" + "{\"_source\":{\"entity_name\":\"Apple Inc.\",\"entity_id\":\"0000320193\"}},"
				+ "{\"_source\":{\"entity_name\":\"Apple Inc.\",\"entity_id\":\"0000320193\"}}" + "]}}";
	}
}

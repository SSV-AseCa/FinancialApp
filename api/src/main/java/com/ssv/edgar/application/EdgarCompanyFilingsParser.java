package com.ssv.edgar.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.EdgarSecFiling;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is a Spring-managed dependency.")
public class EdgarCompanyFilingsParser {

	private final ObjectMapper objectMapper;

	public List<EdgarSecFiling> parse(String payload) {
		JsonNode recent = readTree(payload).path("filings").path("recent");
		return parseRecentFilings(recent);
	}

	private JsonNode readTree(String payload) {
		try {
			return objectMapper.readTree(payload);
		} catch (java.io.IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private List<EdgarSecFiling> parseRecentFilings(JsonNode recent) {
		List<EdgarSecFiling> filings = new ArrayList<>();
		int count = recent.path("form").size();
		for (int index = 0; index < count; index++) {
			filings.add(filingAt(recent, index));
		}
		return filings;
	}

	private EdgarSecFiling filingAt(JsonNode recent, int index) {
		String formType = textAt(recent, "form", index);
		String filingDate = textAt(recent, "filingDate", index);
		String url = textAt(recent, "primaryDocument", index);
		return new EdgarSecFiling(formType, filingDate, url);
	}

	private String textAt(JsonNode recent, String field, int index) {
		return recent.path(field).path(index).asText();
	}
}

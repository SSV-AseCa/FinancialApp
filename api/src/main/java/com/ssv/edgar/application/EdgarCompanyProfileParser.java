package com.ssv.edgar.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.EdgarCompanyProfile;
import java.io.IOException;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Extracts a company's identity (name and primary ticker) from the top level of
 * an EDGAR submissions document. Returns {@link Optional#empty()} when the
 * payload is absent, not valid JSON, or missing those fields — the
 * {@link com.ssv.edgar.infrastructure.client.EdgarHttpClient} surfaces
 * transport failures as a plain error string rather than throwing, so an
 * unknown CIK is indistinguishable from a non-company document and is treated
 * as "no profile".
 */
@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is a Spring-managed dependency.")
public class EdgarCompanyProfileParser {

	private final ObjectMapper objectMapper;

	public Optional<EdgarCompanyProfile> parse(String payload) {
		return readObject(payload).flatMap(this::toProfile);
	}

	private Optional<EdgarCompanyProfile> toProfile(JsonNode root) {
		String name = text(root.path("name"));
		String ticker = text(root.path("tickers").path(0));
		if (isBlank(name) || isBlank(ticker)) {
			return Optional.empty();
		}
		return Optional.of(new EdgarCompanyProfile(name, ticker));
	}

	private Optional<JsonNode> readObject(String payload) {
		return payload == null ? Optional.empty() : tryReadObject(payload);
	}

	private Optional<JsonNode> tryReadObject(String payload) {
		try {
			JsonNode node = objectMapper.readTree(payload);
			return isObject(node) ? Optional.of(node) : Optional.empty();
		} catch (IOException exception) {
			return Optional.empty();
		}
	}

	private boolean isObject(JsonNode node) {
		return node != null && node.isObject();
	}

	private String text(JsonNode node) {
		return node.isMissingNode() || node.isNull() ? null : node.asText();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

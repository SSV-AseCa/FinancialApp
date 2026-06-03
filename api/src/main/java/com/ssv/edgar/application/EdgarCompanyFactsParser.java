package com.ssv.edgar.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.company.application.EdgarFinancialMetric;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "ObjectMapper is a Spring-managed dependency.")
public class EdgarCompanyFactsParser {

	private final ObjectMapper objectMapper;

	public List<EdgarFinancialMetric> parse(String payload) {
		JsonNode facts = readTree(payload).path("facts").path("us-gaap");
		return parseMetrics(facts);
	}

	private JsonNode readTree(String payload) {
		try {
			return objectMapper.readTree(payload);
		} catch (java.io.IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private List<EdgarFinancialMetric> parseMetrics(JsonNode facts) {
		List<EdgarFinancialMetric> metrics = new ArrayList<>();
		Iterator<Map.Entry<String, JsonNode>> fields = facts.fields();
		while (fields.hasNext()) {
			addMetricUnits(metrics, fields.next());
		}
		return metrics;
	}

	private void addMetricUnits(List<EdgarFinancialMetric> metrics, Map.Entry<String, JsonNode> field) {
		Iterator<Map.Entry<String, JsonNode>> units = field.getValue().path("units").fields();
		while (units.hasNext()) {
			addUnitEntries(metrics, field.getKey(), units.next());
		}
	}

	private void addUnitEntries(List<EdgarFinancialMetric> metrics, String metric, Map.Entry<String, JsonNode> unit) {
		for (JsonNode entry : unit.getValue()) {
			addEntry(metrics, metric, unit.getKey(), entry);
		}
	}

	private void addEntry(List<EdgarFinancialMetric> metrics, String metric, String unit, JsonNode entry) {
		if (entry.hasNonNull("val")) {
			metrics.add(new EdgarFinancialMetric(metric, entry.path("val").decimalValue(), unit, text(entry, "end")));
		}
	}

	private String text(JsonNode node, String field) {
		return node.path(field).isMissingNode() ? null : node.path(field).asText();
	}
}

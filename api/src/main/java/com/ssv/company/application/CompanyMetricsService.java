package com.ssv.company.application;

import com.ssv.company.dto.CurrentCompanyMetrics;
import com.ssv.edgar.application.EdgarCompanyFactsParser;
import com.ssv.edgar.application.EdgarClient;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyMetricsService {

	private final FinancialDataProperties properties;
	private final EdgarClient edgarClient;
	private final EdgarCompanyFactsParser factsParser;

	public CurrentCompanyMetrics currentMetrics(String cik) {
		String payload = fetchFactsPayload(cik);
		List<EdgarFinancialMetric> metrics = factsParser.parse(payload);
		Map<String, EdgarFinancialMetric> latest = latestByMetric(metrics);
		return new CurrentCompanyMetrics(valueFor(latest, "Revenues"), valueFor(latest, "NetIncomeLoss"),
				valueFor(latest, "Assets"), valueFor(latest, "StockholdersEquity"));
	}

	private String fetchFactsPayload(String cik) {
		String path = properties.companyFactsPath().formatted(cik.strip());
		return edgarClient.get(path);
	}

	private Map<String, EdgarFinancialMetric> latestByMetric(List<EdgarFinancialMetric> metrics) {
		return metrics.stream()
				.collect(Collectors.groupingBy(EdgarFinancialMetric::metric,
						Collectors.maxBy(Comparator.comparing(this::periodEnd))))
				.entrySet().stream().filter(entry -> entry.getValue().isPresent())
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
	}

	private String periodEnd(EdgarFinancialMetric metric) {
		return Optional.ofNullable(metric.periodEnd()).orElse("");
	}

	private BigDecimal valueFor(Map<String, EdgarFinancialMetric> map, String key) {
		return Optional.ofNullable(map.get(key)).map(EdgarFinancialMetric::value).orElse(null);
	}
}

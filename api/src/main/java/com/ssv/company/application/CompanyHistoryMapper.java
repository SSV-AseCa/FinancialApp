package com.ssv.company.application;

import com.ssv.company.dto.CompanyHistoryPoint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyHistoryMapper {

	private static final int FALLBACK_YEAR = 1970;
	private static final int FALLBACK_MONTH = 1;
	private static final int FALLBACK_DAY = 1;
	private static final int FALLBACK_HASH_MODULO = 100_000;
	private static final int ISO_YEAR_LENGTH = 4;
	private static final int ISO_YEAR_SEPARATOR_INDEX = 4;
	private static final char ISO_DATE_SEPARATOR = '-';

	private final MetricValueResolver resolver;

	public List<CompanyHistoryPoint> toHistoryPoints(List<EdgarFinancialMetric> metrics) {
		return sortedEntries(groupByPeriod(metrics)).stream().map(entry -> toPoint(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private Map<String, Map<String, BigDecimal>> groupByPeriod(List<EdgarFinancialMetric> metrics) {
		Map<String, Map<String, BigDecimal>> byPeriod = new HashMap<>();
		metrics.stream().filter(metric -> metric.periodEnd() != null).forEach(metric -> addMetric(byPeriod, metric));
		return byPeriod;
	}

	private void addMetric(Map<String, Map<String, BigDecimal>> byPeriod, EdgarFinancialMetric metric) {
		byPeriod.computeIfAbsent(metric.periodEnd(), period -> new HashMap<>()).put(metric.metric(), metric.value());
	}

	private List<Map.Entry<String, Map<String, BigDecimal>>> sortedEntries(
			Map<String, Map<String, BigDecimal>> byPeriod) {
		List<Map.Entry<String, Map<String, BigDecimal>>> entries = new ArrayList<>(byPeriod.entrySet());
		entries.sort(Comparator.comparing(entry -> parseDateKey(entry.getKey())));
		return entries;
	}

	private CompanyHistoryPoint toPoint(String periodEnd, Map<String, BigDecimal> metrics) {
		String periodLabel = extractPeriodLabel(periodEnd);
		BigDecimal revenue = resolver.firstOf(metrics, "Revenues", "SalesRevenueNet", "Revenue", "SalesRevenue");
		BigDecimal netIncome = resolver.firstOf(metrics, "NetIncomeLoss", "ProfitLoss",
				"NetIncomeLossAvailableToCommonStockholdersBasic");
		BigDecimal assets = resolver.firstOf(metrics, "Assets");
		BigDecimal equity = resolver.firstOf(metrics, "StockholdersEquity", "StockholdersEquityIncludingPortion",
				"Equity");
		return new CompanyHistoryPoint(periodLabel, revenue, netIncome, assets, equity);
	}

	private LocalDate parseDateKey(String key) {
		try {
			return LocalDate.parse(key);
		} catch (RuntimeException exception) {
			return fallbackDate(key);
		}
	}

	private LocalDate fallbackDate(String key) {
		return LocalDate.of(FALLBACK_YEAR, FALLBACK_MONTH, FALLBACK_DAY)
				.plusDays(Math.abs(key.hashCode()) % FALLBACK_HASH_MODULO);
	}

	private String extractPeriodLabel(String periodEnd) {
		if (isIsoDate(periodEnd)) {
			return periodEnd.substring(0, ISO_YEAR_LENGTH);
		}
		return periodEnd;
	}

	private boolean isIsoDate(String periodEnd) {
		return periodEnd != null && periodEnd.length() > ISO_YEAR_SEPARATOR_INDEX
				&& periodEnd.charAt(ISO_YEAR_SEPARATOR_INDEX) == ISO_DATE_SEPARATOR;
	}
}

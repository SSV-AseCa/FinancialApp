package com.ssv.company.application;

import com.ssv.company.dto.CompanyHistoryPoint;
import com.ssv.company.domain.Company;
import com.ssv.company.exceptions.CompanyNotFoundException;
import com.ssv.edgar.application.EdgarCompanyFactsParser;
import com.ssv.edgar.application.EdgarClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed dependencies are injected and not exposed.")
public class CompanyHistoryService {

	private final CompanyStore companyStore;
	private final EdgarClient edgarClient;
	private final EdgarCompanyFactsParser factsParser;
	private final FinancialDataProperties properties;

	public List<CompanyHistoryPoint> historyByCik(String cik) {
		String normalized = normalizeCik(cik);
		Company company = companyStore.findByCik(normalized).orElseThrow(() -> new CompanyNotFoundException(cik));

		String path = properties.companyFactsPath().formatted(normalized);
		String payload = edgarClient.get(path);
		List<EdgarFinancialMetric> metrics = factsParser.parse(payload);

		// group by periodEnd -> metric name -> value
		Map<String, Map<String, BigDecimal>> byPeriod = new HashMap<>();
		for (EdgarFinancialMetric m : metrics) {
			String period = m.periodEnd();
			if (period == null) {
				continue;
			}
			byPeriod.computeIfAbsent(period, p -> new HashMap<>()).put(m.metric(), m.value());
		}

		// convert to points with mapped fields
		List<Map.Entry<String, Map<String, BigDecimal>>> entries = new ArrayList<>(byPeriod.entrySet());

		// sort by period ascending (parse as LocalDate when possible)
		entries.sort(Comparator.comparing(e -> parseDateKey(e.getKey())));

		return entries.stream().map(e -> toPoint(e.getKey(), e.getValue())).collect(Collectors.toList());
	}

	private LocalDate parseDateKey(String key) {
		try {
			return LocalDate.parse(key);
		} catch (Exception ex) {
			// fallback to epoch-min with stable ordering using hash
			return LocalDate.of(1970, 1, 1).plusDays(Math.abs(key.hashCode()) % 100000);
		}
	}

	private CompanyHistoryPoint toPoint(String periodEnd, Map<String, BigDecimal> metrics) {
		String periodLabel = extractPeriodLabel(periodEnd);
		BigDecimal revenue = firstOf(metrics, "Revenues", "SalesRevenueNet", "Revenue", "SalesRevenue") ;
		BigDecimal netIncome = firstOf(metrics, "NetIncomeLoss", "ProfitLoss", "NetIncomeLossAvailableToCommonStockholdersBasic");
		BigDecimal assets = firstOf(metrics, "Assets");
		BigDecimal equity = firstOf(metrics, "StockholdersEquity", "StockholdersEquityIncludingPortion", "Equity");
		return new CompanyHistoryPoint(periodLabel, revenue, netIncome, assets, equity);
	}

	private BigDecimal firstOf(Map<String, BigDecimal> metrics, String... keys) {
		for (String k : keys) {
			if (metrics.containsKey(k)) {
				return metrics.get(k);
			}
		}
		// try case-insensitive fallback
		Map<String, BigDecimal> lower = new TreeMap<>();
		for (Map.Entry<String, BigDecimal> e : metrics.entrySet()) {
			lower.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
		}
		for (String k : keys) {
			BigDecimal v = lower.get(k.toLowerCase(Locale.ROOT));
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	private String extractPeriodLabel(String periodEnd) {
		if (periodEnd == null) {
			return null;
		}
		// if ISO date like YYYY-MM-DD -> return year
		if (periodEnd.length() >= 4 && periodEnd.charAt(4) == '-') {
			return periodEnd.substring(0, 4);
		}
		return periodEnd;
	}

	private String normalizeCik(String cik) {
		return "%010d".formatted(Long.parseLong(cik.strip()));
	}
}

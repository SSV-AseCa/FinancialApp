package com.ssv.company.application;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

@Component
public class MetricValueResolver {

    public BigDecimal firstOf(Map<String, BigDecimal> metrics, String... keys) {
        BigDecimal exactMatch = firstExactMatch(metrics, keys);
        return exactMatch != null ? exactMatch : firstCaseInsensitiveMatch(metrics, keys);
    }

    private BigDecimal firstExactMatch(Map<String, BigDecimal> metrics, String... keys) {
        for (String key : keys) {
            BigDecimal value = metrics.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private BigDecimal firstCaseInsensitiveMatch(Map<String, BigDecimal> metrics, String... keys) {
        Map<String, BigDecimal> loweredMetrics = lowerCaseMetrics(metrics);
        return firstExactMatch(loweredMetrics, lowerCaseKeys(keys));
    }

    private Map<String, BigDecimal> lowerCaseMetrics(Map<String, BigDecimal> metrics) {
        Map<String, BigDecimal> lowered = new TreeMap<>();
        metrics.forEach((key, value) -> lowered.put(key.toLowerCase(Locale.ROOT), value));
        return lowered;
    }

    private String[] lowerCaseKeys(String[] keys) {
        String[] lowered = new String[keys.length];
        for (int index = 0; index < keys.length; index++) {
            lowered[index] = keys[index].toLowerCase(Locale.ROOT);
        }
        return lowered;
    }
}
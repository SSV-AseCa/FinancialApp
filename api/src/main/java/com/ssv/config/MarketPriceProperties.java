package com.ssv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "market.prices")
public record MarketPriceProperties(long fetchFrequencyMs, List<String> symbols, String source, String baseUrl,
		String quotePath) {
}

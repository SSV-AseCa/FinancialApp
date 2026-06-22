package com.ssv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market.prices")
public record MarketPriceProperties(String baseUrl, String quotePath, String apiKey, String userAgent) {
}

package com.ssv.edgar.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edgar")
public record EdgarProperties(String baseUrl, String userAgent, String apiKey, String submissionsPath,
		String companyFactsPath, int stalenessDays, String searchBaseUrl, String searchPath, RateLimit rateLimit) {
	public record RateLimit(int maxRequests, long windowMillis) {
	}
}

package com.ssv.edgar.infrastructure.config;

import com.ssv.company.application.FinancialDataProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edgar")
public record EdgarProperties(String baseUrl, String userAgent, String apiKey, String submissionsPath,
		String companyFactsPath, int stalenessDays, RateLimit rateLimit) implements FinancialDataProperties {
	public record RateLimit(int maxRequests, long windowMillis) {
	}
}

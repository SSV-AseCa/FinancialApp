package com.financialapp.edgar.infrastructure.config;

import com.financialapp.edgar.application.EdgarClient;
import com.financialapp.edgar.infrastructure.client.EdgarHttpClient;
import com.financialapp.edgar.infrastructure.client.RateLimitedEdgarClient;
import com.financialapp.edgar.infrastructure.ratelimit.RateLimiter;
import com.financialapp.edgar.infrastructure.ratelimit.SlidingWindowRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration
public class EdgarConfig {

	@Bean
	public EdgarClient edgarClient(EdgarProperties properties) {
		RestClient restClient = RestClient.builder().baseUrl(properties.baseUrl())
				.defaultHeader("User-Agent", properties.userAgent()).build();

		EdgarClient httpClient = new EdgarHttpClient(restClient);

		RateLimiter limiter = new SlidingWindowRateLimiter(properties.rateLimit().maxRequests(),
				properties.rateLimit().windowMillis(), Clock.systemUTC());

		return new RateLimitedEdgarClient(httpClient, limiter);
	}
}

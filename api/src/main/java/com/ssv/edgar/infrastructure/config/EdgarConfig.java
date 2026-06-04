package com.ssv.edgar.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.client.EdgarHttpClient;
import com.ssv.edgar.infrastructure.client.RateLimitedEdgarClient;
import com.ssv.edgar.infrastructure.ratelimit.RateLimiter;
import com.ssv.edgar.infrastructure.ratelimit.SlidingWindowRateLimiter;

@Configuration
public class EdgarConfig {

	@Bean
	public EdgarClient edgarClient(EdgarProperties properties, Clock clock) {
		RestClient restClient = createRestClient(properties);
		return limitedClient(properties, restClient, clock);
	}

	@Bean
	@Qualifier("searchEdgarClient")
	public EdgarClient searchEdgarClient(EdgarProperties properties, Clock clock) {
		RestClient restClient = RestClient.builder().baseUrl(properties.searchBaseUrl())
				.defaultHeader("User-Agent", properties.userAgent()).build();
		return limitedClient(properties, restClient, clock);
	}

	private EdgarClient limitedClient(EdgarProperties properties, RestClient restClient, Clock clock) {
		EdgarClient httpClient = new EdgarHttpClient(restClient);
		RateLimiter limiter = new SlidingWindowRateLimiter(properties.rateLimit().maxRequests(),
				properties.rateLimit().windowMillis(), clock);
		return new RateLimitedEdgarClient(httpClient, limiter);
	}

	private RestClient createRestClient(EdgarProperties properties) {
		return RestClient.builder().baseUrl(properties.baseUrl()).defaultHeader("User-Agent", properties.userAgent())
				.defaultHeader("X-Api-Key", properties.apiKey()).build();
	}
}

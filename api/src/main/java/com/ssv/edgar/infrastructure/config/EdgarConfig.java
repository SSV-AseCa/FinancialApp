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
	public EdgarClient edgarClient(EdgarProperties properties) {
		RestClient restClient = RestClient.builder().baseUrl(properties.baseUrl())
				.defaultHeader("User-Agent", properties.userAgent()).build();
		return new RateLimitedEdgarClient(new EdgarHttpClient(restClient), rateLimiter(properties));
	}

	@Bean
	@Qualifier("searchEdgarClient")
	public EdgarClient searchEdgarClient(EdgarProperties properties) {
		RestClient restClient = RestClient.builder().baseUrl(properties.searchBaseUrl())
				.defaultHeader("User-Agent", properties.userAgent()).build();
		return new RateLimitedEdgarClient(new EdgarHttpClient(restClient), rateLimiter(properties));
	}

	private RateLimiter rateLimiter(EdgarProperties properties) {
		return new SlidingWindowRateLimiter(properties.rateLimit().maxRequests(), properties.rateLimit().windowMillis(),
				Clock.systemUTC());
	}
}

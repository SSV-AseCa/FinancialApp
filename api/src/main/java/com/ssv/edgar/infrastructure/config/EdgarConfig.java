package com.ssv.edgar.infrastructure.config;

import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.client.EdgarHttpClient;
import com.ssv.edgar.infrastructure.client.RateLimitedEdgarClient;
import com.ssv.edgar.infrastructure.ratelimit.RateLimiter;
import com.ssv.edgar.infrastructure.ratelimit.SlidingWindowRateLimiter;
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

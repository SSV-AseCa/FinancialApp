package com.ssv.market.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ssv.cache.application.ResponseCache;
import com.ssv.cache.application.ResponseCacheProperties;
import com.ssv.market.application.MarketDataClient;
import com.ssv.market.infrastructure.client.CachingMarketDataClient;
import com.ssv.market.infrastructure.client.YahooFinanceClient;

@Configuration
public class MarketClientConfig {

	/**
	 * Wraps the live Yahoo Finance client in a durable read-through cache. Marked
	 * {@link Primary} so all collaborators (e.g. MarketPriceService) transparently
	 * use the cached client while {@link YahooFinanceClient} remains the delegate.
	 */
	@Bean
	@Primary
	public MarketDataClient cachingMarketDataClient(YahooFinanceClient delegate, ResponseCache cache,
			ResponseCacheProperties properties) {
		return new CachingMarketDataClient(delegate, cache, properties.marketTtl());
	}
}

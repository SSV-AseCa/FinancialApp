package com.ssv.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-memory cache using ConcurrentMapCache (no TTL, unbounded).
 *
 * This is appropriate for company search results because: EDGAR company data
 * changes infrequently, the query space for a given session is small, and it
 * eliminates redundant EDGAR calls within a JVM lifecycle.
 *
 * Trade-off: entries are never evicted while the app is running. For production
 * at scale, replace with Caffeine + TTL (e.g. 10 minutes).
 */
@Configuration
@EnableCaching
public class CacheConfig {

	public static final String COMPANY_SEARCH_CACHE = "company-search";

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(COMPANY_SEARCH_CACHE);
	}
}

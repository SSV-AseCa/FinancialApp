package com.ssv.edgar.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.ssv.cache.application.ResponseCache;
import com.ssv.cache.application.ResponseCacheProperties;
import com.ssv.cache.infrastructure.persistence.CachedResponseRepository;
import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.client.CachingEdgarClient;

class EdgarConfigTest {

	@Test
	void edgarClientShouldCreateCachedClient() {
		EdgarProperties properties = new EdgarProperties("https://data.sec.gov", "test-agent", "",
				"/submissions/CIK%s.json", "/api/xbrl/companyfacts/CIK%s.json", 1, "https://efts.sec.gov",
				"/LATEST/search-index", new EdgarProperties.RateLimit(10, 1000));

		EdgarConfig config = new EdgarConfig();
		ResponseCache cache = new ResponseCache(mock(CachedResponseRepository.class), Clock.systemUTC());
		ResponseCacheProperties cacheProperties = new ResponseCacheProperties(Duration.ofHours(1), Duration.ofHours(1));

		EdgarClient client = config.edgarClient(properties, Clock.systemUTC(), cache, cacheProperties);

		assertNotNull(client);
		assertInstanceOf(CachingEdgarClient.class, client);
	}
}

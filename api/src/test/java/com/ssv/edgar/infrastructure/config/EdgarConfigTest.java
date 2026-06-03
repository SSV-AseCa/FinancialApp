package com.ssv.edgar.infrastructure.config;

import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.client.RateLimitedEdgarClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EdgarConfigTest {

	@Test
	void edgarClientShouldCreateRateLimitedClient() {
		EdgarProperties properties = new EdgarProperties("https://data.sec.gov", "test-agent",
				"/submissions/CIK%s.json", "https://efts.sec.gov", "/LATEST/search-index",
				new EdgarProperties.RateLimit(10, 1000));

		EdgarConfig config = new EdgarConfig();

		EdgarClient client = config.edgarClient(properties);

		assertNotNull(client);
		assertInstanceOf(RateLimitedEdgarClient.class, client);
	}
}

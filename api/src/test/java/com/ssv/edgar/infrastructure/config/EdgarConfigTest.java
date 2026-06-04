package com.ssv.edgar.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;

import org.junit.jupiter.api.Test;

import com.ssv.edgar.application.EdgarClient;
import com.ssv.edgar.infrastructure.client.RateLimitedEdgarClient;

class EdgarConfigTest {

	@Test
	void edgarClientShouldCreateRateLimitedClient() {
		EdgarProperties properties = new EdgarProperties("https://data.sec.gov", "test-agent", "",
				"/submissions/CIK%s.json", "/api/xbrl/companyfacts/CIK%s.json", 1, "https://efts.sec.gov",
				"/LATEST/search-index", new EdgarProperties.RateLimit(10, 1000));

		EdgarConfig config = new EdgarConfig();

		EdgarClient client = config.edgarClient(properties, Clock.systemUTC());

		assertNotNull(client);
		assertInstanceOf(RateLimitedEdgarClient.class, client);
	}
}

package com.ssv.market.infrastructure.config;

import com.ssv.config.MarketPriceProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketPricePropertiesTest {

	@Test
	void shouldCreateProperties() {
		MarketPriceProperties properties = new MarketPriceProperties("base", "path", "api-key", "agent");

		assertEquals("base", properties.baseUrl());
		assertEquals("path", properties.quotePath());
		assertEquals("api-key", properties.apiKey());
		assertEquals("agent", properties.userAgent());
	}
}

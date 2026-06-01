package com.ssv.market.infrastructure.config;

import java.util.List;

import com.ssv.config.MarketPriceProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketPricePropertiesTest {

	@Test
	void shouldCreateProperties() {
		MarketPriceProperties properties =
				new MarketPriceProperties(1000L, "yahoo-finance", "base", "path", "api-key");

		assertEquals(1000L, properties.fetchFrequencyMs());
		assertEquals("yahoo-finance", properties.source());
		assertEquals("base", properties.baseUrl());
		assertEquals("path", properties.quotePath());
		assertEquals("api-key", properties.apiKey());
	}
}

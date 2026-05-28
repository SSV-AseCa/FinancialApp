package com.ssv.market.infrastructure.config;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketPricePropertiesTest {

	@Test
	void shouldCreateProperties() {
		List<String> symbols = List.of("AAPL", "MSFT");
		MarketPriceProperties properties = new MarketPriceProperties(1000L, symbols, "yahoo-finance", "base", "path");

		assertEquals(1000L, properties.fetchFrequencyMs());
		assertEquals(symbols, properties.symbols());
		assertEquals("yahoo-finance", properties.source());
		assertEquals("base", properties.baseUrl());
		assertEquals("path", properties.quotePath());
	}
}

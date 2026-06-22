package com.ssv.market.fake;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.ssv.market.application.CurrentPriceProvider;
import com.ssv.market.application.HistoricalPriceProvider;

/**
 * Replaces the live {@link CurrentPriceProvider} and
 * {@link HistoricalPriceProvider} in integration tests with in-memory stubs so
 * price reads never reach Yahoo Finance. Every symbol resolves to a default
 * price unless a test stubs a specific one via the autowired
 * {@link FakeCurrentPriceProvider}.
 */
@TestConfiguration
public class StubPriceProviderConfig {

	@Bean
	@Primary
	FakeCurrentPriceProvider currentPriceProvider() {
		return new FakeCurrentPriceProvider().withDefault(new BigDecimal("100.00"));
	}

	@Bean
	@Primary
	HistoricalPriceProvider historicalPriceProvider() {
		return (symbol, date) -> Optional.of(new BigDecimal("100.00"));
	}
}

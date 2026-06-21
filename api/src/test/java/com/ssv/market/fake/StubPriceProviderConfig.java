package com.ssv.market.fake;

import java.math.BigDecimal;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.ssv.market.application.CurrentPriceProvider;

/**
 * Replaces the live {@link CurrentPriceProvider} in integration tests with an
 * in-memory stub so price reads never reach Yahoo Finance. Every symbol
 * resolves to a default price unless a test stubs a specific one via the
 * autowired {@link FakeCurrentPriceProvider}.
 */
@TestConfiguration
public class StubPriceProviderConfig {

	@Bean
	@Primary
	FakeCurrentPriceProvider currentPriceProvider() {
		return new FakeCurrentPriceProvider().withDefault(new BigDecimal("100.00"));
	}
}

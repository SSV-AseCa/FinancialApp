package com.ssv.market.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;

class MarketDataHistoricalPriceProviderTest {

	private static final String SYMBOL = "AAPL";
	private static final LocalDate DATE = LocalDate.of(2024, 6, 3);

	private final MarketDataClient client = mock(MarketDataClient.class);
	private final MarketDataHistoricalPriceProvider provider = new MarketDataHistoricalPriceProvider(client);

	@Test
	void returnsPriceWhenClientHasAHistoricalQuote() {
		when(client.fetchPriceAt(SYMBOL, DATE)).thenReturn(new MarketPriceQuote(SYMBOL, new BigDecimal("110"), "USD"));

		Optional<BigDecimal> price = provider.priceAt(SYMBOL, DATE);

		assertEquals(Optional.of(new BigDecimal("110")), price);
	}

	@Test
	void returnsEmptyWhenClientCannotFetchTheHistoricalQuote() {
		when(client.fetchPriceAt(SYMBOL, DATE)).thenThrow(new MarketPriceFetchException("rate limited"));

		Optional<BigDecimal> price = provider.priceAt(SYMBOL, DATE);

		assertTrue(price.isEmpty());
	}
}

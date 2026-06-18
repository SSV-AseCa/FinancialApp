package com.ssv.market.infrastructure.client;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.cache.application.ResponseCache;
import com.ssv.market.application.MarketDataClient;
import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;

/**
 * Read-through cache decorator for {@link MarketDataClient}. A fresh cached
 * quote (within the configured TTL) is served from the DB without calling Yahoo
 * Finance. Transparent — the {@code fetchPrice(symbol)} contract is unchanged;
 * the quote is (de)serialized to JSON for the generic cache store.
 */
public class CachingMarketDataClient implements MarketDataClient {

	private static final String PROVIDER = "YAHOO";

	private final MarketDataClient delegate;
	private final ResponseCache cache;
	private final ObjectMapper mapper = new ObjectMapper();
	private final Duration ttl;

	public CachingMarketDataClient(MarketDataClient delegate, ResponseCache cache, Duration ttl) {
		this.delegate = delegate;
		this.cache = cache;
		this.ttl = ttl;
	}

	@Override
	public MarketPriceQuote fetchPrice(String symbol) {
		String payload = cache.getOrLoad(PROVIDER, symbol, ttl, () -> serialize(delegate.fetchPrice(symbol)));
		return deserialize(payload);
	}

	private String serialize(MarketPriceQuote quote) {
		try {
			return mapper.writeValueAsString(quote);
		} catch (Exception exception) {
			throw new MarketPriceFetchException("Could not serialize market price for caching", exception);
		}
	}

	private MarketPriceQuote deserialize(String payload) {
		try {
			return mapper.readValue(payload, MarketPriceQuote.class);
		} catch (Exception exception) {
			throw new MarketPriceFetchException("Could not read cached market price", exception);
		}
	}
}

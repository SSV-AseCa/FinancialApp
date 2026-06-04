package com.ssv.market.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssv.market.application.service.MarketPriceService;
import com.ssv.market.domain.MarketPrice;

public class FakeMarketPriceService extends MarketPriceService {

	private final List<String> fetchedSymbols = new ArrayList<>();
	private final Map<String, RuntimeException> throwFor = new HashMap<>();

	public FakeMarketPriceService() {
		super(null, null, null, null);
	}

	@Override
	public MarketPrice fetchAndStore(String symbol) {
		if (throwFor.containsKey(symbol)) {
			throw throwFor.get(symbol);
		}
		fetchedSymbols.add(symbol);
		return null;
	}

	public void throwOnFetch(String symbol, RuntimeException ex) {
		throwFor.put(symbol, ex);
	}

	public List<String> fetchedSymbols() {
		return fetchedSymbols;
	}
}

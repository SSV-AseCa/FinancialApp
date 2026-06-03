package com.ssv.market.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.MarketDataClient;
import com.ssv.market.application.dto.MarketPriceQuote;
import org.springframework.stereotype.Service;

import com.ssv.market.domain.MarketPrice;
import com.ssv.market.domain.MarketPriceCreateRequest;
import com.ssv.market.infrastructure.persistence.MarketPriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketPriceService {

	private final MarketDataClient marketDataClient;
	private final MarketPriceRepository repository;
	private final MarketPriceProperties properties;
	private final Clock clock;

	public MarketPrice fetchAndStore(String symbol) {
		MarketPriceQuote quote = marketDataClient.fetchPrice(symbol);
		return repository.save(toEntity(quote));
	}

	public Optional<MarketPrice> getLatestPrice(String symbol) {
		return repository.findTopBySymbolOrderByFetchedAtDesc(symbol);
	}

	private MarketPrice toEntity(MarketPriceQuote quote) {
		MarketPriceCreateRequest request = request(quote);
		return new MarketPrice(request);
	}

	private MarketPriceCreateRequest request(MarketPriceQuote quote) {
		Instant fetchedAt = Instant.now(clock);
		return new MarketPriceCreateRequest(quote.symbol(), quote.price(), quote.currency(), fetchedAt,
				properties.source());
	}
}

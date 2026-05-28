package com.ssv.market.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.MarketDataClient;
import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class YahooFinanceClient implements MarketDataClient {

	private final ObjectMapper mapper;
	private final MarketPriceProperties properties;
	private final RestClient restClient;

	public YahooFinanceClient(
			MarketPriceProperties properties,
			RestClient.Builder builder
	) {
		this.mapper = new ObjectMapper();
		this.properties = properties;
		this.restClient = builder.baseUrl(properties.baseUrl()).build();
	}

	@Override
	public MarketPriceQuote fetchPrice(String symbol) {
		try {
			return parseQuote(symbol, fetchBody(symbol));
		} catch (RuntimeException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new MarketPriceFetchException("Could not fetch market price", exception);
		}
	}

	private String fetchBody(String symbol) {
		return restClient.get().uri(path(symbol)).retrieve().body(String.class);
	}

	private String path(String symbol) {
		return properties.quotePath().formatted(symbol);
	}

	private MarketPriceQuote parseQuote(String symbol, String body) throws java.io.IOException {
		JsonNode meta = result(body).path("meta");
		validatePrice(meta);
		return quote(symbol, meta);
	}

	private JsonNode result(String body) throws java.io.IOException {
		JsonNode chart = mapper.readTree(body).path("chart");
		validateChart(chart);
		return chart.path("result").path(0);
	}

	private void validateChart(JsonNode chart) {
		if (!chart.path("error").isNull()) {
			throw new MarketPriceFetchException("Yahoo Finance returned an error");
		}
	}

	private void validatePrice(JsonNode meta) {
		if (meta.path("regularMarketPrice").isMissingNode()) {
			throw new MarketPriceFetchException("Yahoo Finance response has no price");
		}
	}

	private MarketPriceQuote quote(String symbol, JsonNode meta) {
		BigDecimal price = meta.path("regularMarketPrice").decimalValue();
		String currency = meta.path("currency").asText("USD");
		return new MarketPriceQuote(symbol, price, currency);
	}
}
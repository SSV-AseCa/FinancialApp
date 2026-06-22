package com.ssv.market.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Parses Yahoo Finance {@code /v8/finance/chart} responses. A current quote
 * reads {@code meta.regularMarketPrice}; a historical quote reads the daily
 * {@code close} on or before the requested date from the {@code timestamp} and
 * {@code indicators.quote[0].close} arrays.
 */
class YahooChartParser {

	private final ObjectMapper mapper = new ObjectMapper();

	MarketPriceQuote parseCurrent(String symbol, String body) throws IOException {
		JsonNode meta = result(body).path("meta");
		requirePrice(meta);
		return new MarketPriceQuote(symbol, meta.path("regularMarketPrice").decimalValue(), currency(meta));
	}

	MarketPriceQuote parseHistorical(String symbol, LocalDate date, String body) throws IOException {
		JsonNode result = result(body);
		return new MarketPriceQuote(symbol, closeOnOrBefore(result, date), currency(result.path("meta")));
	}

	private JsonNode result(String body) throws IOException {
		JsonNode chart = mapper.readTree(body).path("chart");
		validateChart(chart);
		JsonNode result = chart.path("result");
		validateResult(result);
		return result.path(0);
	}

	private void validateChart(JsonNode chart) {
		if (!chart.path("error").isNull()) {
			throw new MarketPriceFetchException("Yahoo Finance returned an error");
		}
	}

	private void validateResult(JsonNode result) {
		if (!result.isArray() || result.isEmpty()) {
			throw new MarketPriceFetchException("Yahoo Finance response has no result");
		}
	}

	private void requirePrice(JsonNode meta) {
		if (meta.path("regularMarketPrice").isMissingNode()) {
			throw new MarketPriceFetchException("Yahoo Finance response has no price");
		}
	}

	private String currency(JsonNode node) {
		return node.path("currency").asText("USD");
	}

	private BigDecimal closeOnOrBefore(JsonNode result, LocalDate date) {
		JsonNode timestamps = result.path("timestamp");
		JsonNode closes = result.path("indicators").path("quote").path(0).path("close");
		BigDecimal close = lastCloseUpTo(timestamps, closes, date);
		if (close == null) {
			throw new MarketPriceFetchException("Yahoo Finance has no historical price on or before the date");
		}
		return close;
	}

	private BigDecimal lastCloseUpTo(JsonNode timestamps, JsonNode closes, LocalDate date) {
		BigDecimal found = null;
		for (int i = 0; i < timestamps.size(); i++) {
			if (isUsableBar(timestamps.path(i), closes.path(i), date)) {
				found = closes.path(i).decimalValue();
			}
		}
		return found;
	}

	private boolean isUsableBar(JsonNode timestamp, JsonNode close, LocalDate date) {
		return !close.isNull() && onOrBefore(timestamp, date);
	}

	private boolean onOrBefore(JsonNode timestamp, LocalDate date) {
		LocalDate barDate = Instant.ofEpochSecond(timestamp.asLong()).atZone(ZoneOffset.UTC).toLocalDate();
		return !barDate.isAfter(date);
	}
}

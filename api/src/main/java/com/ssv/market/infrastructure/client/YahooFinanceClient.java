package com.ssv.market.infrastructure.client;

import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.MarketDataClient;
import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class YahooFinanceClient implements MarketDataClient {

	private static final String DAILY_INTERVAL = "1d";
	private static final int LOOKBACK_DAYS = 7;

	private final MarketPriceProperties properties;
	private final RestClient restClient;
	private final YahooChartParser parser = new YahooChartParser();

	public YahooFinanceClient(MarketPriceProperties properties, RestClient.Builder builder) {
		this.properties = properties;
		this.restClient = builder.baseUrl(properties.baseUrl()).build();
	}

	@Override
	public MarketPriceQuote fetchPrice(String symbol) {
		try {
			return parser.parseCurrent(symbol, fetchBody(symbol));
		} catch (MarketPriceFetchException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new MarketPriceFetchException("Could not fetch market price", exception);
		}
	}

	private String fetchBody(String symbol) {
		return restClient.get().uri(path(symbol)).header("User-Agent", properties.userAgent())
				.header("X-Api-Key", properties.apiKey()).retrieve().body(String.class);
	}

	private String path(String symbol) {
		return properties.quotePath().formatted(symbol);
	}

	@Override
	public MarketPriceQuote fetchPriceAt(String symbol, LocalDate date) {
		try {
			return parser.parseHistorical(symbol, date, fetchHistoryBody(symbol, date));
		} catch (MarketPriceFetchException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new MarketPriceFetchException("Could not fetch historical market price", exception);
		}
	}

	private String fetchHistoryBody(String symbol, LocalDate date) {
		return restClient.get().uri(historyPath(symbol, date)).header("User-Agent", properties.userAgent())
				.header("X-Api-Key", properties.apiKey()).retrieve().body(String.class);
	}

	private String historyPath(String symbol, LocalDate date) {
		return path(symbol) + "?period1=" + epochStart(date.minusDays(LOOKBACK_DAYS)) + "&period2="
				+ epochStart(date.plusDays(1)) + "&interval=" + DAILY_INTERVAL;
	}

	private long epochStart(LocalDate date) {
		return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
	}
}

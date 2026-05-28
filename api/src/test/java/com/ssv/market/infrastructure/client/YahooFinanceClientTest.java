package com.ssv.market.infrastructure.client;

import java.util.List;

import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YahooFinanceClientTest {

	private static final String SYMBOL = "AAPL";

	@Test
	void shouldParseYahooFinancePrice() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		YahooFinanceClient client = new YahooFinanceClient(properties(), builder);

		server.expect(requestTo("http://localhost/quote/AAPL"))
				.andRespond(withSuccess(body(), MediaType.APPLICATION_JSON));

		MarketPriceQuote quote = client.fetchPrice(SYMBOL);

		assertEquals(SYMBOL, quote.symbol());
		assertEquals("USD", quote.currency());
		server.verify();
	}

	@Test
	void shouldFailWhenPriceIsMissing() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		YahooFinanceClient client = new YahooFinanceClient(properties(), builder);

		server.expect(requestTo("http://localhost/quote/AAPL"))
				.andRespond(withSuccess(emptyBody(), MediaType.APPLICATION_JSON));

		assertThrows(MarketPriceFetchException.class, () -> client.fetchPrice(SYMBOL));
		server.verify();
	}

	private MarketPriceProperties properties() {
		return new MarketPriceProperties(1000L, List.of(SYMBOL), "yahoo-finance", "http://localhost", "/quote/%s");
	}

	private String body() {
		return "{\"chart\":{\"result\":[{\"meta\":{\"regularMarketPrice\":10,\"currency\":\"USD\"}}],\"error\":null}}";
	}

	private String emptyBody() {
		return "{\"chart\":{\"result\":[{\"meta\":{}}],\"error\":null}}";
	}
}

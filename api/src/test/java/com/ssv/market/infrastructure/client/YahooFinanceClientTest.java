package com.ssv.market.infrastructure.client;

import com.ssv.config.MarketPriceProperties;
import com.ssv.market.application.dto.MarketPriceQuote;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YahooFinanceClientTest {

	private static final String SYMBOL = "AAPL";
	private static final LocalDate OPERATION_DATE = LocalDate.of(2024, 6, 3);

	@Test
	void shouldParseYahooFinancePrice() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		YahooFinanceClient client = new YahooFinanceClient(properties(), builder);

		server.expect(requestTo("http://localhost/quote/AAPL")).andExpect(header("User-Agent", "test-agent"))
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

	@Test
	void shouldReturnCloseOnTheOperationDate() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		YahooFinanceClient client = new YahooFinanceClient(properties(), builder);

		server.expect(requestTo(Matchers.containsString("/quote/AAPL?period1=")))
				.andExpect(header("User-Agent", "test-agent"))
				.andRespond(withSuccess(historyBody(), MediaType.APPLICATION_JSON));

		MarketPriceQuote quote = client.fetchPriceAt(SYMBOL, OPERATION_DATE);

		// 110 is the close on 2024-06-03; the 2024-06-04 bar (120) is ignored
		assertEquals(new BigDecimal("110"), quote.price());
		assertEquals("USD", quote.currency());
		server.verify();
	}

	@Test
	void shouldFailWhenNoBarOnOrBeforeTheOperationDate() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		YahooFinanceClient client = new YahooFinanceClient(properties(), builder);

		server.expect(requestTo(Matchers.containsString("/quote/AAPL?period1=")))
				.andRespond(withSuccess(futureOnlyHistoryBody(), MediaType.APPLICATION_JSON));

		assertThrows(MarketPriceFetchException.class, () -> client.fetchPriceAt(SYMBOL, OPERATION_DATE));
		server.verify();
	}

	private MarketPriceProperties properties() {
		return new MarketPriceProperties("http://localhost", "/quote/%s", "test-api-key", "test-agent");
	}

	private String historyBody() {
		return "{\"chart\":{\"result\":[{\"meta\":{\"currency\":\"USD\"},"
				+ "\"timestamp\":[1717113600,1717372800,1717459200],"
				+ "\"indicators\":{\"quote\":[{\"close\":[100,110,120]}]}}],\"error\":null}}";
	}

	private String futureOnlyHistoryBody() {
		return "{\"chart\":{\"result\":[{\"meta\":{\"currency\":\"USD\"},\"timestamp\":[1717459200],"
				+ "\"indicators\":{\"quote\":[{\"close\":[120]}]}}],\"error\":null}}";
	}

	private String body() {
		return "{\"chart\":{\"result\":[{\"meta\":{\"regularMarketPrice\":10,\"currency\":\"USD\"}}],\"error\":null}}";
	}

	private String emptyBody() {
		return "{\"chart\":{\"result\":[{\"meta\":{}}],\"error\":null}}";
	}
}

package com.financialapp.edgar.infrastructure.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdgarHttpClientTest {

	@Test
	void getShouldReturnResponseBody() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

		server.createContext("/submissions", exchange -> {
			String response = "company-data";
			exchange.sendResponseHeaders(200, response.length());

			try (OutputStream body = exchange.getResponseBody()) {
				body.write(response.getBytes());
			}
		});

		server.start();

		try {
			String baseUrl = "http://localhost:" + server.getAddress().getPort();

			RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();

			EdgarHttpClient client = new EdgarHttpClient(restClient);

			String response = client.get("/submissions");

			assertEquals("company-data", response);
		} finally {
			server.stop(0);
		}
	}

	@Test
	void getShouldReturnExceptionMessageWhenRequestFails() {
		RestClient restClient = RestClient.builder().baseUrl("http://localhost:1").build();

		EdgarHttpClient client = new EdgarHttpClient(restClient);

		String response = client.get("/submissions");

		assertTrue(response != null && !response.isBlank());
	}
}

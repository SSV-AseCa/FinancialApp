package com.ssv.exceptions;

import com.ssv.shared.exceptions.ApiErrorResponse;
import com.ssv.shared.exceptions.EdgarRateLimitException;
import com.ssv.shared.exceptions.GlobalExceptionHandler;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

	@Test
	void handleEdgarRateLimitShouldReturnServiceUnavailableResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		EdgarRateLimitException exception = new EdgarRateLimitException("Rate limit exceeded", new RuntimeException());

		ResponseEntity<ApiErrorResponse> response = handler.handleEdgarRateLimit(exception);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

		Assertions.assertNotNull(response.getBody());
		assertEquals("Rate limit exceeded", response.getBody().message());
	}

	@Test
	void handleMarketPriceFetchShouldReturnServiceUnavailableResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		MarketPriceFetchException exception = new MarketPriceFetchException("No market price available for AAPL");

		ResponseEntity<ApiErrorResponse> response = handler.handleMarketPriceFetch(exception);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

		Assertions.assertNotNull(response.getBody());
		assertEquals("No market price available for AAPL", response.getBody().message());
	}
}

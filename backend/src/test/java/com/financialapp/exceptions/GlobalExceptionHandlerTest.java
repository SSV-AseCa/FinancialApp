package com.financialapp.exceptions;

import com.financialapp.shared.exceptions.ApiErrorResponse;
import com.financialapp.shared.exceptions.EdgarRateLimitException;
import com.financialapp.shared.exceptions.GlobalExceptionHandler;
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
}

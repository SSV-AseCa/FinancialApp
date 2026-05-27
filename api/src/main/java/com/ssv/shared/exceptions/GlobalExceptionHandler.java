package com.ssv.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EdgarRateLimitException.class)
	public ResponseEntity<ApiErrorResponse> handleEdgarRateLimit(EdgarRateLimitException exception) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiErrorResponse(exception.getMessage()));
	}
}

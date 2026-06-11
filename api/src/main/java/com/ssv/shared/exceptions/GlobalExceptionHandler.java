package com.ssv.shared.exceptions;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ssv.portfolio.exceptions.PositionNotFoundException;
import com.ssv.transaction.exceptions.BusinessRuleException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EdgarRateLimitException.class)
	public ResponseEntity<ApiErrorResponse> handleEdgarRateLimit(EdgarRateLimitException exception) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", "));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException exception) {
		String message = "Invalid request body: " + exception.getMostSpecificCause().getMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(message));
	}

	@ExceptionHandler(PositionNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handlePositionNotFound(PositionNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException exception) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(new ApiErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(com.ssv.watchlist.exceptions.DuplicateWatchlistEntryException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateWatchlist(com.ssv.watchlist.exceptions.DuplicateWatchlistEntryException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(exception.getMessage()));
	}
}



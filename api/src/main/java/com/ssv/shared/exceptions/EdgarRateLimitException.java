package com.ssv.shared.exceptions;

public class EdgarRateLimitException extends RuntimeException {

	public EdgarRateLimitException(String message, Throwable cause) {
		super(message, cause);
	}
}

package com.ssv.shared.exceptions;

public class MarketPriceFetchException extends RuntimeException {

	public MarketPriceFetchException(String message) {
		super(message);
	}

	public MarketPriceFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}

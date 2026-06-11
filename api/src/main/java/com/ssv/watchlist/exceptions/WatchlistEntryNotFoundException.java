package com.ssv.watchlist.exceptions;

public class WatchlistEntryNotFoundException extends RuntimeException {
	public WatchlistEntryNotFoundException(String message) {
		super(message);
	}
}

package com.ssv.watchlist.exceptions;

public class DuplicateWatchlistEntryException extends RuntimeException {
    public DuplicateWatchlistEntryException(String message) {
        super(message);
    }
}

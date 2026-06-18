package com.ssv.shared.exceptions;

/**
 * Raised when an EDGAR request cannot be completed — the provider returned an
 * error status or the network call failed. Distinct from a missing company: the
 * CIK may well exist, but EDGAR is temporarily unreachable. Surfaces to clients
 * as HTTP 503 so the failure is not mistaken for a 5xx in our own code.
 */
public class EdgarUnavailableException extends RuntimeException {

	public EdgarUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}

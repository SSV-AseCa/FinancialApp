package com.ssv.company.domain;

public final class CikUtils {

	private CikUtils() {
		// Utility class
	}

	public static String normalize(String cik) {
		if (cik == null) {
			throw new IllegalArgumentException("Invalid CIK");
		}
		try {
			return "%010d".formatted(Long.parseLong(cik.strip()));
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid CIK", exception);
		}
	}
}

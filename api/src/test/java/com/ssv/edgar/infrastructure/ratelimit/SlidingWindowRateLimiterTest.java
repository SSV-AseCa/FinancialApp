package com.ssv.edgar.infrastructure.ratelimit;

import com.ssv.edgar.infrastructure.ratelimit.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SlidingWindowRateLimiterTest {

	@Test
	void acquireShouldAllowRequestsWhenThereIsCapacity() {
		Clock clock = Clock.fixed(Instant.ofEpochMilli(1000), ZoneId.of("UTC"));

		SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(2, 1000, clock);

		assertDoesNotThrow(rateLimiter::acquire);
		assertDoesNotThrow(rateLimiter::acquire);
	}

	@Test
	void acquireShouldAllowRequestWhenPreviousRequestsAreExpired() {
		Clock clock = Clock.fixed(Instant.ofEpochMilli(2000), ZoneId.of("UTC"));

		SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(1, 1, clock);

		assertDoesNotThrow(rateLimiter::acquire);
	}
}

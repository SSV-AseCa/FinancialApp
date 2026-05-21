package com.ssv.edgar.infrastructure.ratelimit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


class SlidingWindowRateLimiterTest {

	@Test
	void acquireShouldAllowRequestsWhenThereIsCapacity() {
		Clock clock = Clock.fixed(Instant.ofEpochMilli(1000), ZoneId.of("UTC"));
		SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(2, 1000, clock);

		assertDoesNotThrow(rateLimiter::acquire);
		assertDoesNotThrow(rateLimiter::acquire);
	}

	@Test
	void acquireShouldWaitWhenThereIsNoCapacity() throws Exception {
		MutableClock clock = new MutableClock(1000);
		SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(1, 30, clock);

		rateLimiter.acquire();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> secondRequest = executor.submit(rateLimiter::acquire);

		Assertions.assertFalse(secondRequest.isDone());

		clock.setMillis(1031);

		assertDoesNotThrow(() -> secondRequest.get(1, TimeUnit.SECONDS));
		executor.shutdownNow();
	}

	@Test
	void acquireShouldAllowRequestWhenPreviousRequestsAreExpired() {
		MutableClock clock = new MutableClock(1000);
		SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(1, 1000, clock);

		rateLimiter.acquire();
		clock.setMillis(2001);

		assertDoesNotThrow(rateLimiter::acquire);
	}

	@Test
	void acquireShouldThrowWhenWaitingThreadIsInterrupted() throws Exception {
		Clock clock = Clock.fixed(Instant.ofEpochMilli(1000), ZoneId.of("UTC"));
		SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(1, 60_000, clock);

		rateLimiter.acquire();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> blockedRequest = executor.submit(rateLimiter::acquire);

		Thread.sleep(50);
		executor.shutdownNow();

		ExecutionException exception = assertThrows(ExecutionException.class, blockedRequest::get);

		assertInstanceOf(IllegalStateException.class, exception.getCause());
	}

	private static class MutableClock extends Clock {

		private long millis;

		MutableClock(long millis) {
			this.millis = millis;
		}

		void setMillis(long millis) {
			this.millis = millis;
		}

		@Override
		public ZoneId getZone() {
			return ZoneId.of("UTC");
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return Instant.ofEpochMilli(millis);
		}
	}
}

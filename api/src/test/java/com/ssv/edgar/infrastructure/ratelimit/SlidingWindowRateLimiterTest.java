package com.ssv.edgar.infrastructure.ratelimit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
	@Test
	void shouldLimitRequestsUnderConcurrentAccess() throws Exception {
		SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(10, 1000);

		int threads = 20;
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);

		List<Long> executionTimes = Collections.synchronizedList(new ArrayList<>());

		for (int i = 0; i < threads; i++) {
			executor.submit(() -> {
				try {
					start.await();

					limiter.acquire();

					executionTimes.add(System.currentTimeMillis());
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			});
		}

		long testStart = System.currentTimeMillis();

		start.countDown();
		done.await();

		executor.shutdown();

		long firstSecondCount = executionTimes.stream()
				.filter(time -> time - testStart < 1000)
				.count();

		assertThat(firstSecondCount).isLessThanOrEqualTo(10);
	}
}

package com.ssv.edgar.infrastructure.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SlidingWindowRateLimiterConcurrencyTest {

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

		long firstSecondCount = executionTimes.stream().filter(time -> time - testStart < 1000).count();

		assertThat(firstSecondCount).isLessThanOrEqualTo(10);
	}
}

package com.financialapp.edgar.infrastructure.ratelimit;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;

@RequiredArgsConstructor
public class SlidingWindowRateLimiter implements RateLimiter {

	private final int maxRequests;
	private final long windowMillis;
	private final Clock clock;
	private final Deque<Long> requests = new ArrayDeque<>();

	@Override
	public synchronized void acquire() {
		while (!hasCapacity()) {
			waitUntilCapacity();
		}

		requests.addLast(now());
		notifyAll();
	}

	private boolean hasCapacity() {
		removeExpiredRequests();
		return requests.size() < maxRequests;
	}

	private void removeExpiredRequests() {
		long oldestAllowed = now() - windowMillis;

		while (!requests.isEmpty() && requests.peekFirst() <= oldestAllowed) {
			requests.removeFirst();
		}
	}

	private void waitUntilCapacity() {
		long waitMillis = millisUntilNextSlot();
		try {
			wait(Math.max(waitMillis, 1));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for EDGAR rate limit", exception);
		}
	}

	private long millisUntilNextSlot() {
		Long oldestRequest = requests.peekFirst();

		if (oldestRequest == null) {
			return windowMillis;
		}

		long elapsedMillis = now() - oldestRequest;
		long remainingMillis = windowMillis - elapsedMillis;

		return Math.max(remainingMillis, 1);
	}

	private long now() {
		return clock.millis();
	}
}

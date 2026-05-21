package com.ssv.edgar.infrastructure.ratelimit;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class MutableClock extends Clock {

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

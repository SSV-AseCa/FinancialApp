package com.ssv.portfolio.exceptions;

import java.util.UUID;

public class PositionNotFoundException extends RuntimeException {

	public PositionNotFoundException(UUID positionId) {
		super("Position not found: " + positionId);
	}
}

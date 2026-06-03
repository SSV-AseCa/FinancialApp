package com.ssv.portfolio.dto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record PortfolioResponse(UUID id, List<PositionResponse> positions) {

	public PortfolioResponse {
		positions = Collections.unmodifiableList(positions);
	}
}

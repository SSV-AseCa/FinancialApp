package com.ssv.portfolio.fake;

import java.util.UUID;

import com.ssv.portfolio.application.PortfolioPerformanceService;
import com.ssv.portfolio.dto.PortfolioPerformanceResponse;

public class FakePortfolioPerformanceService extends PortfolioPerformanceService {

	private PortfolioPerformanceResponse response;

	public FakePortfolioPerformanceService() {
		super(null, null, null);
	}

	public void respondWith(PortfolioPerformanceResponse r) {
		this.response = r;
	}

	public void reset() {
		response = null;
	}

	@Override
	public PortfolioPerformanceResponse getPortfolioPerformance(UUID investorId) {
		return response;
	}
}

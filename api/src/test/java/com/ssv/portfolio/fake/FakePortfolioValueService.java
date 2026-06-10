package com.ssv.portfolio.fake;

import java.util.UUID;

import com.ssv.portfolio.application.PortfolioValueService;
import com.ssv.portfolio.dto.PortfolioValueResponse;

public class FakePortfolioValueService extends PortfolioValueService {

	private PortfolioValueResponse response;

	public FakePortfolioValueService() {
		super(null, null, null);
	}

	public void respondWith(PortfolioValueResponse r) {
		this.response = r;
	}

	public void reset() {
		response = null;
	}

	@Override
	public PortfolioValueResponse getPortfolioValue(UUID investorId) {
		return response;
	}
}

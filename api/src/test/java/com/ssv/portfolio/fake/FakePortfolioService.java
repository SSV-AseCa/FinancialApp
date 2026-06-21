package com.ssv.portfolio.fake;

import java.util.UUID;

import com.ssv.portfolio.application.PortfolioService;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;

public class FakePortfolioService extends PortfolioService {

	private PortfolioResponse portfolioResponse;
	private PositionResponse positionResponse;
	private RuntimeException error;

	public FakePortfolioService() {
		super(null, null, null);
	}

	public void respondWithPortfolio(PortfolioResponse r) {
		this.portfolioResponse = r;
	}

	public void respondWithPosition(PositionResponse r) {
		this.positionResponse = r;
	}

	public void throwOnNextCall(RuntimeException e) {
		this.error = e;
	}

	public void reset() {
		portfolioResponse = null;
		positionResponse = null;
		error = null;
	}

	@Override
	public PortfolioResponse getPortfolio(UUID investorId) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
		return portfolioResponse;
	}

	@Override
	public PositionResponse addPosition(UUID investorId, AddPositionRequest req) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
		return positionResponse;
	}

	@Override
	public PositionResponse updatePosition(UUID investorId, UUID positionId, AddPositionRequest req) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
		return positionResponse;
	}

	@Override
	public void removePosition(UUID investorId, UUID positionId) {
		if (error != null) {
			RuntimeException e = error;
			error = null;
			throw e;
		}
	}
}

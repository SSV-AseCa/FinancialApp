package com.ssv.portfolio.fake;

import java.util.Collections;
import java.util.List;

import com.ssv.portfolio.application.PortfolioPositionQueryService;

public class FakePortfolioPositionQueryService extends PortfolioPositionQueryService {

	private List<String> symbols = Collections.emptyList();

	public FakePortfolioPositionQueryService() {
		super(null);
	}

	public void respondWith(List<String> symbols) {
		this.symbols = symbols;
	}

	@Override
	public List<String> findDistinctSymbols() {
		return symbols;
	}
}

package com.ssv.portfolio.application;

import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioPositionQueryService {

	private final PositionRepository repository;

	public List<String> findDistinctSymbols() {
		return repository.findDistinctTickers();
	}
}

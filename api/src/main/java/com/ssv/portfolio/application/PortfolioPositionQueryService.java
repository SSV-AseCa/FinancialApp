package com.ssv.portfolio.application;

import com.ssv.portfolio.infrastructure.persistence.PortfolioPositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioPositionQueryService {

	private final PortfolioPositionRepository repository;

	public List<String> findDistinctSymbols() {
		return repository.findDistinctSymbols();
	}
}

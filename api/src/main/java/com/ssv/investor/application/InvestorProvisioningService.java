package com.ssv.investor.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssv.investor.domain.Investor;
import com.ssv.investor.infrastructure.persistence.InvestorRepository;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvestorProvisioningService {

	private final InvestorRepository investorRepository;
	private final PortfolioRepository portfolioRepository;

	@Transactional
	public UUID provisionIfAbsent(String auth0Sub) {
		return investorRepository.findByAuth0Sub(auth0Sub).map(Investor::getId)
				.orElseGet(() -> createWithPortfolio(auth0Sub));
	}

	private UUID createWithPortfolio(String auth0Sub) {
		Investor investor = investorRepository.save(newInvestor(auth0Sub));
		portfolioRepository.save(newPortfolio(investor.getId()));
		return investor.getId();
	}

	private Investor newInvestor(String auth0Sub) {
		Investor investor = new Investor();
		investor.setAuth0Sub(auth0Sub);
		return investor;
	}

	private Portfolio newPortfolio(UUID investorId) {
		Portfolio portfolio = new Portfolio();
		portfolio.setInvestorId(investorId);
		return portfolio;
	}
}

package com.ssv.portfolio.infrastructure.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.application.PortfolioService;
import com.ssv.portfolio.dto.PortfolioResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

	private final PortfolioService portfolioService;

	@GetMapping
	public ResponseEntity<PortfolioResponse> getPortfolio(HttpServletRequest request) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.ok(portfolioService.getPortfolio(investorId));
	}
}

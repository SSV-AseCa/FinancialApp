package com.ssv.portfolio.infrastructure.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.application.PortfolioValueService;
import com.ssv.portfolio.dto.PortfolioValueResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioValueController {

	private final PortfolioValueService portfolioValueService;

	@GetMapping("/value")
	public ResponseEntity<PortfolioValueResponse> getPortfolioValue(HttpServletRequest request) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.ok(portfolioValueService.getPortfolioValue(investorId));
	}
}

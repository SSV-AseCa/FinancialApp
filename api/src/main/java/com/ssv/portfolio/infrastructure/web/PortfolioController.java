package com.ssv.portfolio.infrastructure.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.application.PortfolioService;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

	@PostMapping("/positions")
	public ResponseEntity<PositionResponse> addPosition(HttpServletRequest request,
			@Valid @RequestBody AddPositionRequest body) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.addPosition(investorId, body));
	}
}

package com.ssv.portfolio.infrastructure.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.application.PortfolioService;
import com.ssv.portfolio.dto.AddPositionRequest;
import com.ssv.portfolio.dto.ModifyPositionRequest;
import com.ssv.portfolio.dto.PortfolioResponse;
import com.ssv.portfolio.dto.PositionResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PortfolioController {

	private final PortfolioService portfolioService;

	@GetMapping("/portfolio")
	public ResponseEntity<PortfolioResponse> getPortfolio(HttpServletRequest request) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.ok(portfolioService.getPortfolio(investorId));
	}

	@PostMapping("/portfolio/positions")
	public ResponseEntity<PositionResponse> addPosition(HttpServletRequest request,
			@Valid @RequestBody AddPositionRequest body) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.addPosition(investorId, body));
	}

	@PutMapping("/portfolio/positions/{positionId}")
	public ResponseEntity<PositionResponse> updatePosition(HttpServletRequest request, @PathVariable UUID positionId,
			@Valid @RequestBody ModifyPositionRequest body) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.ok(portfolioService.updatePosition(investorId, positionId, body));
	}

	@DeleteMapping("/portfolio/positions/{positionId}")
	public ResponseEntity<Void> removePosition(HttpServletRequest request, @PathVariable UUID positionId) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		portfolioService.removePosition(investorId, positionId);
		return ResponseEntity.noContent().build();
	}
}

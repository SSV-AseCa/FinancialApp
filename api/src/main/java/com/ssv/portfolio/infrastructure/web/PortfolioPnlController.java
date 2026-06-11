package com.ssv.portfolio.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.portfolio.application.PortfolioPnlService;
import com.ssv.portfolio.dto.PositionPnlResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioPnlController {

	private final PortfolioPnlService pnlService;

	@GetMapping("/positions/pnl")
	public ResponseEntity<List<PositionPnlResponse>> getPositionsPnl(HttpServletRequest request) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.ok(pnlService.getPositionsPnl(investorId));
	}
}

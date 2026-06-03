package com.ssv.transaction.infrastructure.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.transaction.application.TransactionService;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.SellRequest;
import com.ssv.transaction.dto.TransactionResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/portfolio/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping("/buy")
	public ResponseEntity<TransactionResponse> buy(HttpServletRequest request, @Valid @RequestBody BuyRequest body) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.buy(investorId, body));
	}

	@PostMapping("/sell")
	public ResponseEntity<TransactionResponse> sell(HttpServletRequest request, @Valid @RequestBody SellRequest body) {
		UUID investorId = (UUID) request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR);
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.sell(investorId, body));
	}
}

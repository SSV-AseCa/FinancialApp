package com.ssv.transaction.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

	private final PortfolioRepository portfolioRepository;
	private final TransactionRepository transactionRepository;

	public List<TransactionResponse> getHistory(UUID investorId) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		return transactionRepository.findByPortfolioIdOrderByTransactionDateDescCreatedAtDesc(portfolio.getId())
				.stream().map(this::toResponse).toList();
	}

	private TransactionResponse toResponse(Transaction t) {
		return new TransactionResponse(t.getId(), t.getPortfolioId(), t.getCik(), t.getQuantity(), t.getType(),
				t.getTransactionDate());
	}
}

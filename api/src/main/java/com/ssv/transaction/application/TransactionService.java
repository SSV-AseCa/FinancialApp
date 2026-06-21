package com.ssv.transaction.application;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.SellRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

	private final PortfolioRepository portfolioRepository;
	private final TransactionRepository transactionRepository;
	private final CompanyProvisioningService companyProvisioningService;
	private final PositionMutator positionMutator;

	public TransactionResponse buy(UUID investorId, BuyRequest request) {
		UUID portfolioId = requirePortfolioId(investorId);
		Transaction tx = newTransaction(portfolioId, request.cik(), request.quantity(), TransactionType.BUY);
		transactionRepository.save(tx);
		positionMutator.increase(portfolioId, resolveSymbol(request.cik()), request.quantity());
		return toResponse(tx);
	}

	public TransactionResponse sell(UUID investorId, SellRequest request) {
		UUID portfolioId = requirePortfolioId(investorId);
		positionMutator.decrease(portfolioId, resolveSymbol(request.cik()), request.quantity(), request.cik());
		Transaction tx = newTransaction(portfolioId, request.cik(), request.quantity(), TransactionType.SELL);
		transactionRepository.save(tx);
		return toResponse(tx);
	}

	private UUID requirePortfolioId(UUID investorId) {
		var portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
		return portfolio.getId();
	}

	/**
	 * Positions are keyed by the Yahoo-native ticker symbol (the same key market
	 * prices use), so a CIK-based trade is resolved to its symbol before touching
	 * the position.
	 */
	private String resolveSymbol(String cik) {
		return companyProvisioningService.ensureCompany(cik).getSymbol();
	}

	private Transaction newTransaction(UUID portfolioId, String cik, int quantity, TransactionType type) {
		Transaction tx = new Transaction();
		tx.setPortfolioId(portfolioId);
		tx.setCik(cik);
		tx.setQuantity(quantity);
		tx.setType(type);
		tx.setTransactionDate(LocalDate.now());
		return tx;
	}

	private TransactionResponse toResponse(Transaction tx) {
		return new TransactionResponse(tx.getId(), tx.getPortfolioId(), tx.getCik(), tx.getQuantity(), tx.getType(),
				tx.getTransactionDate());
	}
}

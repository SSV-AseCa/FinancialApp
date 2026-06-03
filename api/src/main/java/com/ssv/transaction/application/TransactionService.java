package com.ssv.transaction.application;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.domain.TransactionType;
import com.ssv.transaction.dto.BuyRequest;
import com.ssv.transaction.dto.SellRequest;
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.exceptions.BusinessRuleException;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final TransactionRepository transactionRepository;

	public TransactionResponse buy(UUID investorId, BuyRequest request) {
		Portfolio portfolio = requirePortfolio(investorId);
		Transaction tx = newTransaction(portfolio.getId(), request.cik(), request.quantity(), TransactionType.BUY);
		transactionRepository.save(tx);
		createOrIncreasePosition(portfolio.getId(), request.cik(), request.quantity());
		return new TransactionResponse(tx.getId(), tx.getPortfolioId(), tx.getCik(), tx.getQuantity(), tx.getType(),
				tx.getTransactionDate());
	}

	public TransactionResponse sell(UUID investorId, SellRequest request) {
		Portfolio portfolio = requirePortfolio(investorId);
		Position position = positionRepository.findByPortfolioIdAndTicker(portfolio.getId(), request.cik())
				.orElseThrow(() -> new BusinessRuleException("No position found for CIK " + request.cik()));
		Transaction tx = newTransaction(portfolio.getId(), request.cik(), request.quantity(), TransactionType.SELL);
		transactionRepository.save(tx);
		decreaseOrRemovePosition(position, request.quantity());
		return new TransactionResponse(tx.getId(), tx.getPortfolioId(), tx.getCik(), tx.getQuantity(), tx.getType(),
				tx.getTransactionDate());
	}

	private Portfolio requirePortfolio(UUID investorId) {
		return portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));
	}

	private void validateSufficientShares(Position position, int requested) {
		if (position.getQuantity() < requested) {
			throw new BusinessRuleException(
					"Insufficient shares: holds " + position.getQuantity() + " but requested " + requested);
		}
	}

	private void createOrIncreasePosition(UUID portfolioId, String cik, int quantity) {
		positionRepository.findByPortfolioIdAndTicker(portfolioId, cik).ifPresentOrElse(p -> {
			p.setQuantity(p.getQuantity() + quantity);
			positionRepository.save(p);
		}, () -> positionRepository.save(newPosition(portfolioId, cik, quantity)));
	}

	private void decreaseOrRemovePosition(Position position, int quantity) {
		validateSufficientShares(position, quantity);
		if (position.getQuantity() == quantity) {
			positionRepository.delete(position);
		} else {
			position.setQuantity(position.getQuantity() - quantity);
			positionRepository.save(position);
		}
	}

	private Transaction newTransaction(UUID portfolioId, String cik, int quantity, TransactionType type) {
		Transaction t = new Transaction();
		t.setPortfolioId(portfolioId);
		t.setCik(cik);
		t.setQuantity(quantity);
		t.setType(type);
		t.setTransactionDate(LocalDate.now());
		return t;
	}

	private Position newPosition(UUID portfolioId, String cik, int quantity) {
		Position p = new Position();
		p.setPortfolioId(portfolioId);
		p.setTicker(cik);
		p.setQuantity(quantity);
		p.setOperationDate(LocalDate.now());
		return p;
	}
}

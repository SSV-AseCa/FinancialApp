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
import com.ssv.transaction.dto.TransactionResponse;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final PortfolioRepository portfolioRepository;
	private final PositionRepository positionRepository;
	private final TransactionRepository transactionRepository;

	@Transactional
	public TransactionResponse buy(UUID investorId, BuyRequest request) {
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId)
				.orElseThrow(() -> new IllegalStateException("No portfolio found for investor " + investorId));

		Transaction transaction = buildTransaction(portfolio.getId(), request);
		transactionRepository.save(transaction);

		createOrIncreasePosition(portfolio.getId(), request.cik(), request.quantity());

		return toResponse(transaction);
	}

	private void createOrIncreasePosition(UUID portfolioId, String cik, int quantity) {
		positionRepository.findByPortfolioIdAndTicker(portfolioId, cik).ifPresentOrElse(position -> {
			position.setQuantity(position.getQuantity() + quantity);
			positionRepository.save(position);
		}, () -> positionRepository.save(newPosition(portfolioId, cik, quantity)));
	}

	private Transaction buildTransaction(UUID portfolioId, BuyRequest request) {
		Transaction t = new Transaction();
		t.setPortfolioId(portfolioId);
		t.setCik(request.cik());
		t.setQuantity(request.quantity());
		t.setType(TransactionType.BUY);
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

	private TransactionResponse toResponse(Transaction t) {
		return new TransactionResponse(t.getId(), t.getPortfolioId(), t.getCik(), t.getQuantity(), t.getType(),
				t.getTransactionDate());
	}
}

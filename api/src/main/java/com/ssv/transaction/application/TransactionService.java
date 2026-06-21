package com.ssv.transaction.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssv.company.application.CompanyProvisioningService;
import com.ssv.market.application.CurrentPriceProvider;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.shared.exceptions.MarketPriceFetchException;
import com.ssv.transaction.domain.Transaction;
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
	private final CurrentPriceProvider currentPriceProvider;

	public TransactionResponse buy(UUID investorId, BuyRequest request) {
		UUID portfolioId = requirePortfolioId(investorId);
		String symbol = resolveSymbol(request.cik());
		BigDecimal unitPrice = requirePrice(symbol);
		Transaction tx = Transaction.buy(portfolioId, request.cik(), request.quantity(), unitPrice);
		transactionRepository.save(tx);
		positionMutator.increase(portfolioId, symbol, request.quantity(), unitPrice);
		return toResponse(tx);
	}

	public TransactionResponse sell(UUID investorId, SellRequest request) {
		UUID portfolioId = requirePortfolioId(investorId);
		String symbol = resolveSymbol(request.cik());
		positionMutator.decrease(portfolioId, symbol, request.quantity(), request.cik());
		BigDecimal salePrice = currentPriceProvider.currentPrice(symbol).orElse(null);
		Transaction tx = Transaction.sell(portfolioId, request.cik(), request.quantity(), salePrice);
		transactionRepository.save(tx);
		return toResponse(tx);
	}

	/**
	 * A buy must record the price paid, so an unavailable market price blocks the
	 * trade rather than persisting a position with unknown cost basis.
	 */
	private BigDecimal requirePrice(String symbol) {
		return currentPriceProvider.currentPrice(symbol)
				.orElseThrow(() -> new MarketPriceFetchException("No market price available for " + symbol));
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

	private TransactionResponse toResponse(Transaction tx) {
		return new TransactionResponse(tx.getId(), tx.getPortfolioId(), tx.getCik(), tx.getQuantity(), tx.getType(),
				tx.getTransactionDate());
	}
}

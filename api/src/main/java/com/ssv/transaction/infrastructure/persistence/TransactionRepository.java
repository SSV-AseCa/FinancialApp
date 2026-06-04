package com.ssv.transaction.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.transaction.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
	List<Transaction> findByPortfolioId(UUID portfolioId);

	List<Transaction> findByPortfolioIdOrderByTransactionDateDescCreatedAtDesc(UUID portfolioId);
}

package com.ssv.transaction.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ssv.fake.JpaRepositoryBase;
import com.ssv.transaction.domain.Transaction;
import com.ssv.transaction.infrastructure.persistence.TransactionRepository;

public class FakeTransactionRepository extends JpaRepositoryBase<Transaction, UUID> implements TransactionRepository {

	public final List<Transaction> store = new ArrayList<>();

	@Override
	public List<Transaction> findByPortfolioId(UUID portfolioId) {
		return store.stream().filter(t -> portfolioId.equals(t.getPortfolioId())).toList();
	}

	@Override
	public List<Transaction> findByPortfolioIdOrderByTransactionDateDescCreatedAtDesc(UUID portfolioId) {
		return findByPortfolioId(portfolioId);
	}

	@Override
	public <S extends Transaction> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId(UUID.randomUUID());
		}
		store.add(entity);
		return entity;
	}
}

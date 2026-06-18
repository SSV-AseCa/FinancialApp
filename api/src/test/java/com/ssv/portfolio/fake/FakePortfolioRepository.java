package com.ssv.portfolio.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ssv.fake.JpaRepositoryBase;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;

public class FakePortfolioRepository extends JpaRepositoryBase<Portfolio, UUID> implements PortfolioRepository {

	private final Map<UUID, Portfolio> byId = new HashMap<>();
	private final Map<UUID, Portfolio> byInvestorId = new HashMap<>();
	private Portfolio lastSaved;

	@Override
	public Optional<Portfolio> findByInvestorId(UUID investorId) {
		return Optional.ofNullable(byInvestorId.get(investorId));
	}

	@Override
	public boolean existsByInvestorId(UUID investorId) {
		return byInvestorId.containsKey(investorId);
	}

	@Override
	public <S extends Portfolio> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId(UUID.randomUUID());
		}
		byId.put(entity.getId(), entity);
		byInvestorId.put(entity.getInvestorId(), entity);
		lastSaved = entity;
		return entity;
	}

	public void seed(Portfolio p) {
		byId.put(p.getId(), p);
		byInvestorId.put(p.getInvestorId(), p);
	}

	public Portfolio lastSaved() {
		return lastSaved;
	}

	public Map<UUID, Portfolio> savedById() {
		return byId;
	}
}

package com.ssv.portfolio.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ssv.fake.JpaRepositoryBase;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

public class FakePositionRepository extends JpaRepositoryBase<Position, UUID> implements PositionRepository {

	private final Map<UUID, Position> store = new HashMap<>();
	private final List<Position> deleted = new ArrayList<>();
	private Position lastSaved;

	@Override
	public List<Position> findByPortfolioId(UUID portfolioId) {
		return store.values().stream().filter(p -> portfolioId.equals(p.getPortfolioId())).toList();
	}

	@Override
	public Optional<Position> findByIdAndPortfolioId(UUID id, UUID portfolioId) {
		return Optional.ofNullable(store.get(id)).filter(p -> portfolioId.equals(p.getPortfolioId()));
	}

	@Override
	public Optional<Position> findByPortfolioIdAndTicker(UUID portfolioId, String ticker) {
		return store.values().stream()
				.filter(p -> portfolioId.equals(p.getPortfolioId()) && ticker.equals(p.getTicker())).findFirst();
	}

	@Override
	public List<String> findDistinctTickers() {
		return store.values().stream().map(p -> p.getTicker().toUpperCase(java.util.Locale.ROOT)).distinct().toList();
	}

	@Override
	public <S extends Position> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId(UUID.randomUUID());
		}
		store.put(entity.getId(), entity);
		lastSaved = entity;
		return entity;
	}

	@Override
	public void delete(Position entity) {
		store.remove(entity.getId());
		deleted.add(entity);
	}

	public void seed(Position p) {
		store.put(p.getId(), p);
	}

	public boolean wasDeleted(Position p) {
		return deleted.stream().anyMatch(d -> d.getId().equals(p.getId()));
	}

	public Position lastSaved() {
		return lastSaved;
	}

	public List<Position> deletedPositions() {
		return deleted;
	}
}

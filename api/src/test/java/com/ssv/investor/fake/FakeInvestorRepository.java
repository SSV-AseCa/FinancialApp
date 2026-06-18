package com.ssv.investor.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ssv.fake.JpaRepositoryBase;
import com.ssv.investor.domain.Investor;
import com.ssv.investor.infrastructure.persistence.InvestorRepository;

public class FakeInvestorRepository extends JpaRepositoryBase<Investor, UUID> implements InvestorRepository {

	private final Map<UUID, Investor> store = new HashMap<>();

	@Override
	public Optional<Investor> findByAuth0Sub(String auth0Sub) {
		return store.values().stream().filter(i -> auth0Sub.equals(i.getAuth0Sub())).findFirst();
	}

	@Override
	public <S extends Investor> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId(UUID.randomUUID());
		}
		store.put(entity.getId(), entity);
		return entity;
	}
}

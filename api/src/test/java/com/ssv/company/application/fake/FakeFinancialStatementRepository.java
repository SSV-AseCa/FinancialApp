package com.ssv.company.application.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.infrastructure.persistence.FinancialStatementRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.function.Function;

public class FakeFinancialStatementRepository implements FinancialStatementRepository {

	private final Map<UUID, List<FinancialStatement>> byCompanyId = new HashMap<>();
	private final List<FinancialStatement> all = new ArrayList<>();
	private int deleteCallCount;

	public void seed(UUID companyId, List<FinancialStatement> statements) {
		byCompanyId.put(companyId, new ArrayList<>(statements));
		all.addAll(statements);
	}

	@Override
	public void deleteByCompanyId(UUID companyId) {
		deleteCallCount++;
		byCompanyId.remove(companyId);
	}

	@Override
	public List<FinancialStatement> findByCompanyId(UUID companyId) {
		return byCompanyId.getOrDefault(companyId, List.of());
	}

	@Override
	public <S extends FinancialStatement> List<S> saveAll(Iterable<S> statements) {
		List<S> result = new ArrayList<>();
		statements.forEach(s -> {
			all.add(s);
			result.add(s);
		});
		return result;
	}

	public boolean wasDeleteCalled() {
		return deleteCallCount > 0;
	}

	// --- Unsupported JpaRepository stubs ---

	@Override
	public <S extends FinancialStatement> S save(S entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<FinancialStatement> findById(UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean existsById(UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FinancialStatement> findAll() {
		return List.copyOf(all);
	}

	@Override
	public List<FinancialStatement> findAllById(Iterable<UUID> uuids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long count() {
		return all.size();
	}

	@Override
	public void deleteById(UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(FinancialStatement entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllById(Iterable<? extends UUID> uuids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll(Iterable<? extends FinancialStatement> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FinancialStatement> findAll(Sort sort) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Page<FinancialStatement> findAll(Pageable pageable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush() {
	}

	@Override
	public <S extends FinancialStatement> S saveAndFlush(S entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> List<S> saveAllAndFlush(Iterable<S> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllInBatch(Iterable<FinancialStatement> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<UUID> uuids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllInBatch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FinancialStatement getOne(UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FinancialStatement getById(UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FinancialStatement getReferenceById(UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> Optional<S> findOne(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> List<S> findAll(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> List<S> findAll(Example<S> example, Sort sort) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> Page<S> findAll(Example<S> example, Pageable pageable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> long count(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement> boolean exists(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends FinancialStatement, R> R findBy(Example<S> example,
			Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
		throw new UnsupportedOperationException();
	}
}

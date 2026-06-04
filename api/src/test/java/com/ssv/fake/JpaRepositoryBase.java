package com.ssv.fake;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

public abstract class JpaRepositoryBase<T, ID> implements JpaRepository<T, ID> {

	// --- JpaRepository ---

	@Override
	public void flush() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> S saveAndFlush(S entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllInBatch(Iterable<T> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<ID> ids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllInBatch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T getReferenceById(ID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("deprecation")
	public T getById(ID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("deprecation")
	public T getOne(ID id) {
		throw new UnsupportedOperationException();
	}

	// --- QueryByExampleExecutor ---

	@Override
	public <S extends T> Optional<S> findOne(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> long count(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> boolean exists(Example<S> example) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T, R> R findBy(Example<S> example,
			Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
		throw new UnsupportedOperationException();
	}

	// --- ListCrudRepository ---

	@Override
	public abstract <S extends T> S save(S entity);

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<T> findById(ID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean existsById(ID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> findAll() {
		return Collections.emptyList();
	}

	@Override
	public List<T> findAllById(Iterable<ID> ids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public void deleteById(ID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(T entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAllById(Iterable<? extends ID> ids) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

	// --- ListPagingAndSortingRepository ---

	@Override
	public List<T> findAll(Sort sort) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		throw new UnsupportedOperationException();
	}
}

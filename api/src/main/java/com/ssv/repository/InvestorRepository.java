package com.ssv.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.entity.Investor;

public interface InvestorRepository extends JpaRepository<Investor, UUID> {
	Optional<Investor> findByAuth0Sub(String auth0Sub);
}

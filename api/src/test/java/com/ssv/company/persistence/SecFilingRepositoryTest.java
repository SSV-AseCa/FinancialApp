package com.ssv.company.persistence;

import com.ssv.company.domain.Company;
import com.ssv.company.domain.FinancialStatement;
import com.ssv.company.domain.FinancialStatementCreateRequest;
import com.ssv.company.domain.SecFiling;
import com.ssv.company.domain.SecFilingCreateRequest;
import com.ssv.company.infrastructure.persistence.CompanyRepository;
import com.ssv.company.infrastructure.persistence.FinancialStatementRepository;
import com.ssv.company.infrastructure.persistence.SecFilingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class SecFilingRepositoryTest {

    @Autowired
    private SecFilingRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deletesFilingsByCompanyId() {

        Company company =
                entityManager.persistAndFlush(
                        new Company(
                                "0000320193",
                                "AAPL",
                                "Apple"));

        entityManager.persistAndFlush(
                new SecFiling(
                        new SecFilingCreateRequest(
                                company,
                                "10-K",
                                "2025-01-01",
                                "https://sec.gov",
                                Instant.now())));

        repository.deleteByCompanyId(company.getId());

        entityManager.flush();

        assertTrue(
                repository.findAll().isEmpty());
    }
}
package com.ssv.company.persistence;

import com.ssv.company.domain.Company;
import com.ssv.company.infrastructure.persistence.CompanyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository repository;

    @Test
    void findsCompanyByCik() {
        Company company =
                repository.save(
                        new Company(
                                "0000320193",
                                "AAPL",
                                "Apple Inc."));

        Optional<Company> found =
                repository.findByCik("0000320193");

        Assertions.assertTrue(found.isPresent());
        assertEquals(company.getCik(), found.get().getCik());
    }
}
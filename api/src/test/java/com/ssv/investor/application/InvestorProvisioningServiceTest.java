package com.ssv.investor.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssv.investor.domain.Investor;
import com.ssv.investor.fake.FakeInvestorRepository;
import com.ssv.portfolio.fake.FakePortfolioRepository;

class InvestorProvisioningServiceTest {

	private static final String SUB = "auth0|abc123";

	private FakeInvestorRepository fakeInvestorRepo;
	private FakePortfolioRepository fakePortfolioRepo;
	private InvestorProvisioningService service;

	@BeforeEach
	void setUp() {
		fakeInvestorRepo = new FakeInvestorRepository();
		fakePortfolioRepo = new FakePortfolioRepository();
		service = new InvestorProvisioningService(fakeInvestorRepo, fakePortfolioRepo);
	}

	@Test
	void returnsExistingInvestorId() {
		Investor existing = investorWithRandomId();
		existing.setAuth0Sub(SUB);
		fakeInvestorRepo.save(existing);

		assertEquals(existing.getId(), service.provisionIfAbsent(SUB));
		assertTrue(fakePortfolioRepo.savedById().isEmpty());
	}

	@Test
	void createsInvestorAndPortfolioWhenAbsent() {
		UUID id = service.provisionIfAbsent(SUB);

		assertNotNull(id);
		assertFalse(fakePortfolioRepo.savedById().isEmpty());
	}

	@Test
	void portfolioLinkedToCreatedInvestor() {
		UUID id = service.provisionIfAbsent(SUB);

		assertEquals(id, fakePortfolioRepo.lastSaved().getInvestorId());
	}

	private static Investor investorWithRandomId() {
		Investor investor = new Investor();
		investor.setId(UUID.randomUUID());
		return investor;
	}
}

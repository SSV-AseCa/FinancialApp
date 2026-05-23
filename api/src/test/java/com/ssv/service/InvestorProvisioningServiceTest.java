package com.ssv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ssv.entity.Investor;
import com.ssv.entity.Portfolio;
import com.ssv.repository.InvestorRepository;
import com.ssv.repository.PortfolioRepository;

@ExtendWith(MockitoExtension.class)
class InvestorProvisioningServiceTest {

	private static final String SUB = "auth0|abc123";

	@Mock
	private InvestorRepository investorRepository;

	@Mock
	private PortfolioRepository portfolioRepository;

	@InjectMocks
	private InvestorProvisioningService service;

	@Test
	void returnsExistingInvestorId() {
		Investor existing = investorWithRandomId();
		when(investorRepository.findByAuth0Sub(SUB)).thenReturn(Optional.of(existing));
		assertEquals(existing.getId(), service.provisionIfAbsent(SUB));
		verifyNoInteractions(portfolioRepository);
	}

	@Test
	void createsInvestorAndPortfolioWhenAbsent() {
		when(investorRepository.findByAuth0Sub(SUB)).thenReturn(Optional.empty());
		when(investorRepository.save(any(Investor.class))).thenAnswer(inv -> assignId(inv.getArgument(0)));
		UUID id = service.provisionIfAbsent(SUB);
		assertNotNull(id);
		verify(portfolioRepository).save(any(Portfolio.class));
	}

	@Test
	void portfolioLinkedToCreatedInvestor() {
		when(investorRepository.findByAuth0Sub(SUB)).thenReturn(Optional.empty());
		when(investorRepository.save(any(Investor.class))).thenAnswer(inv -> assignId(inv.getArgument(0)));
		UUID id = service.provisionIfAbsent(SUB);
		ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
		verify(portfolioRepository).save(captor.capture());
		assertEquals(id, captor.getValue().getInvestorId());
	}

	private static Investor investorWithRandomId() {
		Investor investor = new Investor();
		investor.setId(UUID.randomUUID());
		return investor;
	}

	private static Investor assignId(Investor investor) {
		investor.setId(UUID.randomUUID());
		return investor;
	}
}

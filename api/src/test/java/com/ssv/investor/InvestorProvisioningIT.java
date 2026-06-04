package com.ssv.investor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.ssv.TestcontainersConfiguration;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.investor.infrastructure.persistence.InvestorRepository;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;

@Import({TestcontainersConfiguration.class, InvestorProvisioningIT.MockJwtConfig.class})
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true",
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class InvestorProvisioningIT {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	@Autowired
	private InvestorProvisioningService service;

	@Autowired
	private InvestorRepository investorRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Test
	void createsInvestorAndPortfolioOnFirstRequest() {
		String sub = "auth0|it-new-user";
		UUID id = service.provisionIfAbsent(sub);
		assertNotNull(id);
		assertTrue(investorRepository.findByAuth0Sub(sub).isPresent());
		assertTrue(portfolioRepository.existsByInvestorId(id));
	}

	@Test
	void idempotentOnSubsequentRequests() {
		String sub = "auth0|it-repeat-user";
		UUID id1 = service.provisionIfAbsent(sub);
		UUID id2 = service.provisionIfAbsent(sub);
		assertEquals(id1, id2);
	}
}

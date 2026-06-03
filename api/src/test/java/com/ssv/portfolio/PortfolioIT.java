package com.ssv.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.TestcontainersConfiguration;
import com.ssv.entity.Portfolio;
import com.ssv.entity.Position;
import com.ssv.repository.PortfolioRepository;
import com.ssv.repository.PositionRepository;
import com.ssv.service.InvestorProvisioningService;

@Import({TestcontainersConfiguration.class, PortfolioIT.MockJwtConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PortfolioIT {

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
	private MockMvc mockMvc;
	@Autowired
	private InvestorProvisioningService provisioningService;
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PositionRepository positionRepository;

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(get("/portfolio")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsEmptyPortfolioForNewInvestor() throws Exception {
		String sub = "auth0|portfolio-it-empty-" + UUID.randomUUID();
		Portfolio portfolio = provisionAndGetPortfolio(sub);
		mockMvc.perform(jwtRequest(sub)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(portfolio.getId().toString()))
				.andExpect(jsonPath("$.positions").isArray()).andExpect(jsonPath("$.positions").isEmpty());
	}

	@Test
	void returnsPortfolioWithPositions() throws Exception {
		String sub = "auth0|portfolio-it-with-positions-" + UUID.randomUUID();
		Portfolio portfolio = provisionAndGetPortfolio(sub);
		positionRepository.save(buildPosition(portfolio.getId(), "MSFT", 5, LocalDate.of(2024, 3, 20)));
		mockMvc.perform(jwtRequest(sub)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(portfolio.getId().toString()))
				.andExpect(jsonPath("$.positions[0].ticker").value("MSFT"))
				.andExpect(jsonPath("$.positions[0].quantity").value(5))
				.andExpect(jsonPath("$.positions[0].operationDate").value("2024-03-20"));
	}

	@Test
	void investorCannotSeeAnotherInvestorsPortfolio() throws Exception {
		String subA = "auth0|portfolio-it-investor-a-" + UUID.randomUUID();
		String subB = "auth0|portfolio-it-investor-b-" + UUID.randomUUID();
		Portfolio portfolioA = provisionAndGetPortfolio(subA);
		Portfolio portfolioB = provisionAndGetPortfolio(subB);
		mockMvc.perform(jwtRequest(subA)).andExpect(jsonPath("$.id").value(portfolioA.getId().toString()));
		mockMvc.perform(jwtRequest(subB)).andExpect(jsonPath("$.id").value(portfolioB.getId().toString()));
	}

	private Portfolio provisionAndGetPortfolio(String sub) {
		UUID investorId = provisioningService.provisionIfAbsent(sub);
		return portfolioRepository.findByInvestorId(investorId).orElseThrow();
	}

	private static Position buildPosition(UUID portfolioId, String ticker, int qty, LocalDate date) {
		Position p = new Position();
		p.setPortfolioId(portfolioId);
		p.setTicker(ticker);
		p.setQuantity(qty);
		p.setOperationDate(date);
		return p;
	}

	private MockHttpServletRequestBuilder jwtRequest(String sub) {
		return get("/portfolio").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)));
	}
}

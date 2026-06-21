package com.ssv.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.TestcontainersConfiguration;
import com.ssv.company.application.CompanyStore;
import com.ssv.company.domain.Company;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;
import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.market.fake.StubPriceProviderConfig;

@Import({TestcontainersConfiguration.class, UpdatePositionIT.MockJwtConfig.class, StubPriceProviderConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class UpdatePositionIT {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	private static final String MSFT_CIK = "0000789019";
	private static final String AAPL_CIK = "0000320193";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private InvestorProvisioningService provisioningService;
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PositionRepository positionRepository;
	@Autowired
	private CompanyStore companyRepository;

	@BeforeEach
	void seedCompanies() {
		seedCompany(MSFT_CIK, "MSFT", "Microsoft Corp.");
		seedCompany(AAPL_CIK, "AAPL", "Apple Inc.");
	}

	private void seedCompany(String cik, String symbol, String name) {
		if (companyRepository.findByCik(cik).isEmpty()) {
			companyRepository.save(new Company(cik, symbol, name));
		}
	}

	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(put("/portfolio/positions/" + UUID.randomUUID())
				.with(SecurityMockMvcRequestPostProcessors.csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"cik\":\"" + AAPL_CIK + "\",\"quantity\":10,\"operationDate\":\"2024-01-15\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void returns200WithUpdatedPosition() throws Exception {
		String sub = "auth0|update-position-it-" + UUID.randomUUID();
		Position position = createPosition(sub, "AAPL", 5, LocalDate.of(2024, 1, 1));

		mockMvc.perform(authenticatedPut(sub, position.getId(), MSFT_CIK, 20, "2024-06-01")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(position.getId().toString()))
				.andExpect(jsonPath("$.ticker").value("MSFT")).andExpect(jsonPath("$.quantity").value(20))
				.andExpect(jsonPath("$.operationDate").value("2024-06-01"));
	}

	@Test
	void returns404WhenPositionBelongsToAnotherInvestor() throws Exception {
		String subOwner = "auth0|update-position-owner-" + UUID.randomUUID();
		String subOther = "auth0|update-position-other-" + UUID.randomUUID();
		Position position = createPosition(subOwner, "AAPL", 5, LocalDate.of(2024, 1, 1));
		provisioningService.provisionIfAbsent(subOther);

		mockMvc.perform(authenticatedPut(subOther, position.getId(), MSFT_CIK, 10, "2024-06-01"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns404WhenPositionDoesNotExist() throws Exception {
		String sub = "auth0|update-position-noexist-" + UUID.randomUUID();
		provisioningService.provisionIfAbsent(sub);

		mockMvc.perform(authenticatedPut(sub, UUID.randomUUID(), AAPL_CIK, 5, "2024-01-15"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenCikIsBlank() throws Exception {
		String sub = "auth0|update-position-blank-" + UUID.randomUUID();
		Position position = createPosition(sub, "AAPL", 5, LocalDate.of(2024, 1, 1));
		mockMvc.perform(authenticatedPut(sub, position.getId(), "  ", 10, "2024-01-15"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	@Test
	void returns400WhenQuantityIsNotPositive() throws Exception {
		String sub = "auth0|update-position-qty-" + UUID.randomUUID();
		Position position = createPosition(sub, "AAPL", 5, LocalDate.of(2024, 1, 1));
		mockMvc.perform(authenticatedPut(sub, position.getId(), AAPL_CIK, 0, "2024-01-15"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
	}

	private Position createPosition(String sub, String ticker, int qty, LocalDate date) {
		UUID investorId = provisioningService.provisionIfAbsent(sub);
		Portfolio portfolio = portfolioRepository.findByInvestorId(investorId).orElseThrow();
		Position p = new Position();
		p.setPortfolioId(portfolio.getId());
		p.setTicker(ticker);
		p.setQuantity(qty);
		p.setOperationDate(date);
		return positionRepository.save(p);
	}

	private MockHttpServletRequestBuilder authenticatedPut(String sub, UUID positionId, String cik, int qty,
			String date) {
		String body = "{\"cik\":\"%s\",\"quantity\":%d,\"operationDate\":\"%s\"}".formatted(cik, qty, date);
		return put("/portfolio/positions/" + positionId)
				.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(sub)))
				.contentType(MediaType.APPLICATION_JSON).content(body);
	}
}

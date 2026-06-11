package com.ssv.portfolio.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;
import com.ssv.market.fake.FakeMarketPriceRepository;
import com.ssv.market.infrastructure.persistence.MarketPriceRepository;
import com.ssv.portfolio.application.PortfolioPnlService;
import com.ssv.portfolio.domain.Portfolio;
import com.ssv.portfolio.domain.Position;
import com.ssv.portfolio.fake.FakePortfolioRepository;
import com.ssv.portfolio.fake.FakePositionRepository;
import com.ssv.portfolio.infrastructure.persistence.PortfolioRepository;
import com.ssv.portfolio.infrastructure.persistence.PositionRepository;

@WebMvcTest(PortfolioPnlController.class)
@Import(PortfolioPnlControllerTest.Config.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class PortfolioPnlControllerTest {

	@TestConfiguration
	static class Config {

		@Bean
		PortfolioRepository portfolioRepository() {
			return new FakePortfolioRepository();
		}

		@Bean
		PositionRepository positionRepository() {
			return new FakePositionRepository();
		}

		@Bean
		MarketPriceRepository marketPriceRepository() {
			return new FakeMarketPriceRepository();
		}

		@Bean
		PortfolioPnlService pnlService(PortfolioRepository pr, PositionRepository pos, MarketPriceRepository mr) {
			return new PortfolioPnlService(pr, pos, mr);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FakePortfolioRepository portfolioRepository;

	@Autowired
	private FakePositionRepository positionRepository;

	@Autowired
	private FakeMarketPriceRepository marketPriceRepository;

	@Test
	void returns401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get("/portfolio/positions/pnl")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsPositionsWithPnlForAuthenticatedUser() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();

		Portfolio p = new Portfolio();
		p.setId(portfolioId);
		p.setInvestorId(investorId);
		portfolioRepository.seed(p);

		Position pos = new Position();
		pos.setId(positionId);
		pos.setPortfolioId(portfolioId);
		pos.setTicker("AAPL");
		pos.setQuantity(10);
		pos.setOperationDate(LocalDate.of(2024, 1, 15));
		pos.setCostBasis(BigDecimal.valueOf(100));
		positionRepository.seed(pos);

		marketPriceRepository.stubLatestPrice("AAPL", BigDecimal.valueOf(120));

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(positionId.toString()))
				.andExpect(jsonPath("$[0].ticker").value("AAPL")).andExpect(jsonPath("$[0].quantity").value(10))
				.andExpect(jsonPath("$[0].costBasis").value(100)).andExpect(jsonPath("$[0].currentPrice").value(120))
				.andExpect(jsonPath("$[0].currentValue").value(1200.00)).andExpect(jsonPath("$[0].pnl").value(200.00));
	}

	@Test
	void missingStoredMarketPriceReturnsNullValuationFields() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		UUID positionId = UUID.randomUUID();

		Portfolio p = new Portfolio();
		p.setId(portfolioId);
		p.setInvestorId(investorId);
		portfolioRepository.seed(p);

		Position pos = new Position();
		pos.setId(positionId);
		pos.setPortfolioId(portfolioId);
		pos.setTicker("MSFT");
		pos.setQuantity(5);
		pos.setOperationDate(LocalDate.of(2024, 1, 15));
		pos.setCostBasis(BigDecimal.valueOf(50));
		positionRepository.seed(pos);

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentPrice").doesNotExist())
				.andExpect(jsonPath("$[0].currentValue").doesNotExist()).andExpect(jsonPath("$[0].pnl").doesNotExist());
	}

	@Test
	void emptyPortfolioReturnsEmptyList() throws Exception {
		UUID investorId = UUID.randomUUID();
		UUID portfolioId = UUID.randomUUID();
		Portfolio p = new Portfolio();
		p.setId(portfolioId);
		p.setInvestorId(investorId);
		portfolioRepository.seed(p);

		mockMvc.perform(authenticatedRequest(investorId)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
	}

	private MockHttpServletRequestBuilder authenticatedRequest(UUID investorId) {
		return get("/portfolio/positions/pnl").with(SecurityMockMvcRequestPostProcessors.jwt())
				.requestAttr(InvestorProvisioningFilter.INVESTOR_ID_ATTR, investorId);
	}
}

package com.ssv.investor.infrastructure.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.ssv.investor.fake.FakeInvestorProvisioningService;

class InvestorProvisioningFilterTest {

	private static final String SUB = "auth0|filter-test";

	private FakeInvestorProvisioningService service;
	private InvestorProvisioningFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private FilterChainSpy chain;

	@BeforeEach
	void setUp() {
		service = new FakeInvestorProvisioningService();
		filter = new InvestorProvisioningFilter(service);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		chain = new FilterChainSpy();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void setsInvestorIdWhenJwtAuthenticated() throws Exception {
		UUID id = UUID.randomUUID();
		service.respondWith(id);
		JwtAuthenticationToken auth = jwtToken(SUB);
		SecurityContextHolder.getContext().setAuthentication(auth);

		filter.doFilter(request, response, chain);

		assertEquals(id, request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR));
		assertTrue(chain.invoked);
	}

	@Test
	void skipsProvisioningWhenNotAuthenticated() throws Exception {
		filter.doFilter(request, response, chain);

		assertNull(request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR));
		assertFalse(service.called);
		assertTrue(chain.invoked);
	}

	@Test
	void chainAlwaysInvoked() throws Exception {
		filter.doFilter(request, response, chain);

		assertTrue(chain.invoked);
	}

	private static JwtAuthenticationToken jwtToken(String sub) {
		Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").claim("sub", sub)
				.expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now()).build();
		return new JwtAuthenticationToken(jwt);
	}

	static class FilterChainSpy implements FilterChain {

		boolean invoked;

		@Override
		public void doFilter(ServletRequest req, ServletResponse res) {
			invoked = true;
		}
	}
}

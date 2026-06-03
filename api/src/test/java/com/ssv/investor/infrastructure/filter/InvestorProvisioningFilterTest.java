package com.ssv.investor.infrastructure.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.ssv.investor.application.InvestorProvisioningService;

import jakarta.servlet.FilterChain;

class InvestorProvisioningFilterTest {

	private static final String SUB = "auth0|filter-test";

	private InvestorProvisioningService service;
	private InvestorProvisioningFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private FilterChain chain;

	@BeforeEach
	void setUp() {
		service = mock(InvestorProvisioningService.class);
		filter = new InvestorProvisioningFilter(service);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		chain = mock(FilterChain.class);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void setsInvestorIdWhenJwtAuthenticated() throws Exception {
		UUID id = UUID.randomUUID();
		JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
		when(auth.getName()).thenReturn(SUB);
		when(service.provisionIfAbsent(SUB)).thenReturn(id);
		SecurityContextHolder.getContext().setAuthentication(auth);
		filter.doFilter(request, response, chain);
		assertEquals(id, request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR));
		verify(chain).doFilter(request, response);
	}

	@Test
	void skipsProvisioningWhenNotAuthenticated() throws Exception {
		filter.doFilter(request, response, chain);
		assertNull(request.getAttribute(InvestorProvisioningFilter.INVESTOR_ID_ATTR));
		verifyNoInteractions(service);
		verify(chain).doFilter(request, response);
	}

	@Test
	void chainAlwaysInvoked() throws Exception {
		filter.doFilter(request, response, chain);
		verify(chain).doFilter(request, response);
	}
}

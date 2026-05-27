package com.ssv.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ssv.config.AuthUtils;
import com.ssv.service.InvestorProvisioningService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InvestorProvisioningFilter extends OncePerRequestFilter {

	public static final String INVESTOR_ID_ATTR = "investorId";

	private final InvestorProvisioningService provisioningService;

	public InvestorProvisioningFilter(InvestorProvisioningService provisioningService) {
		this.provisioningService = provisioningService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtToken) {
			UUID investorId = provisioningService.provisionIfAbsent(AuthUtils.extractSub(jwtToken));
			request.setAttribute(INVESTOR_ID_ATTR, investorId);
		}
		chain.doFilter(request, response);
	}
}

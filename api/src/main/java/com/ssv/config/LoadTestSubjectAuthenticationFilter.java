package com.ssv.config;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Establishes investor identity from a request header instead of a verified
 * JWT. Used ONLY under the {@code loadtest-nojwt} Spring profile, where the
 * resource server is disabled so a load-test client can drive the API without
 * minting tokens. The {@code X-Loadtest-Subject} header value becomes the JWT
 * {@code sub}, so rotating it simulates N isolated investors exactly as the
 * signed-token path does. It synthesizes the {@link JwtAuthenticationToken}
 * that
 * {@link com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter}
 * already consumes, so the provisioning filter and controllers stay unchanged.
 *
 * <p>
 * <strong>Security:</strong> this trusts an unauthenticated header and MUST
 * NEVER be activated in a released artifact — it is a local/CI-on-demand
 * load-testing convenience only.
 */
public class LoadTestSubjectAuthenticationFilter extends OncePerRequestFilter {

	public static final String SUBJECT_HEADER = "X-Loadtest-Subject";

	private static final String DEFAULT_SUBJECT = "loadtest-anonymous";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String subject = request.getHeader(SUBJECT_HEADER);
		if (!StringUtils.hasText(subject)) {
			subject = DEFAULT_SUBJECT;
		}
		Jwt jwt = Jwt.withTokenValue("loadtest-" + subject).header("alg", "none").subject(subject).build();
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
		chain.doFilter(request, response);
	}
}

package com.ssv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;

/**
 * Security chain for the {@code loadtest-nojwt} profile: every request is
 * permitted and NO JWT resource server is registered, so a load-test client can
 * drive the API without minting or verifying tokens. Investor identity is taken
 * from the {@code X-Loadtest-Subject} header by
 * {@link LoadTestSubjectAuthenticationFilter}, which runs before the existing
 * {@link InvestorProvisioningFilter} and feeds it the same
 * {@code JwtAuthenticationToken} the signed-token path produces.
 *
 * <p>
 * This replaces {@link SecurityConfig} (gated
 * {@code @Profile("!loadtest-nojwt")}) only under this profile. It MUST NEVER
 * be activated in a released artifact.
 */
@Configuration
@EnableWebSecurity
@Profile("loadtest-nojwt")
public class LoadTestOpenSecurityConfig {

	private final InvestorProvisioningService provisioningService;

	public LoadTestOpenSecurityConfig(InvestorProvisioningService provisioningService) {
		this.provisioningService = provisioningService;
	}

	@Bean
	public SecurityFilterChain loadTestFilterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.addFilterBefore(new LoadTestSubjectAuthenticationFilter(), AuthorizationFilter.class)
				.addFilterBefore(buildProvisioningFilter(), AuthorizationFilter.class);
		return http.build();
	}

	private InvestorProvisioningFilter buildProvisioningFilter() {
		return new InvestorProvisioningFilter(provisioningService);
	}
}

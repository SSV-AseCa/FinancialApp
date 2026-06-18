package com.ssv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final InvestorProvisioningService provisioningService;

	public SecurityConfig(InvestorProvisioningService provisioningService) {
		this.provisioningService = provisioningService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(this::configureAuth)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)))
				.addFilterAfter(buildProvisioningFilter(), BearerTokenAuthenticationFilter.class);
		return http.build();
	}

	private InvestorProvisioningFilter buildProvisioningFilter() {
		return new InvestorProvisioningFilter(provisioningService);
	}

	private void configureAuth(AuthorizeHttpRequestsConfigurer<?>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth.requestMatchers("/actuator/health").permitAll().anyRequest().authenticated();
	}
}

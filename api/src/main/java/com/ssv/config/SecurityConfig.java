package com.ssv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.ssv.investor.application.InvestorProvisioningService;
import com.ssv.investor.infrastructure.filter.InvestorProvisioningFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final String audience;
	private final String issuer;
	private final InvestorProvisioningService provisioningService;

	public SecurityConfig(@Value("${auth0.audience}") String audience,
			@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
			InvestorProvisioningService provisioningService) {
		this.audience = audience;
		this.issuer = issuer;
		this.provisioningService = provisioningService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(this::configureAuth)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
				.addFilterAfter(buildProvisioningFilter(), BearerTokenAuthenticationFilter.class);
		return http.build();
	}

	private InvestorProvisioningFilter buildProvisioningFilter() {
		return new InvestorProvisioningFilter(provisioningService);
	}

	private void configureAuth(AuthorizeHttpRequestsConfigurer<?>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth.requestMatchers("/actuator/health").permitAll().anyRequest().authenticated();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		NimbusJwtDecoder decoder = buildDecoder();
		decoder.setJwtValidator(buildValidator());
		return decoder;
	}

	private NimbusJwtDecoder buildDecoder() {
		return (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuer);
	}

	private OAuth2TokenValidator<Jwt> buildValidator() {
		OAuth2Error audienceError = new OAuth2Error("invalid_token", "Invalid audience", null);
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
		OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(audience, audienceError);
		return new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);
	}
}

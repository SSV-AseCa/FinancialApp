package com.ssv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Production JWT decoder: validates Auth0-issued RS256 tokens against the
 * tenant's OIDC metadata, enforcing issuer and the configured audience. This is
 * the default decoder for every profile EXCEPT {@code loadtest}, which
 * substitutes a local offline decoder (see {@link LoadTestJwtDecoderConfig}).
 * The behavior here is identical to the decoder previously inlined in
 * {@link SecurityConfig}.
 */
@Configuration
@Profile("!loadtest")
public class Auth0JwtDecoderConfig {

	private final String audience;
	private final String issuer;

	public Auth0JwtDecoderConfig(@Value("${auth0.audience}") String audience,
			@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
		this.audience = audience;
		this.issuer = issuer;
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

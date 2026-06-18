package com.ssv.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.core.env.Environment;

/**
 * Offline JWT decoder used ONLY under the {@code loadtest} Spring profile. It
 * validates HS256 tokens signed with a shared symmetric secret, so a load-test
 * client (Locust) can mint its own tokens locally without any Auth0/network
 * round-trip. Distinct {@code sub} claims drive lazy investor provisioning, so
 * a single secret simulates N isolated investors.
 *
 * <p>
 * <strong>Security:</strong> this decoder trusts any token signed with the
 * shared secret. It is profile-gated and MUST NEVER be activated in a released
 * artifact — the {@code loadtest} profile is a local/CI-on-demand convenience
 * only.
 */
@Configuration
@Profile("loadtest")
public class LoadTestJwtDecoderConfig {

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final String secret;
	private final String issuer;
	private final String audience;

	public LoadTestJwtDecoderConfig(@Value("${loadtest.jwt.secret}") String secret,
			@Value("${loadtest.jwt.issuer}") String issuer, Environment environment) {
		this.secret = secret;
		this.issuer = issuer;
		this.audience = environment.getProperty("auth0.audience", "");
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
		decoder.setJwtValidator(buildValidator());
		return decoder;
	}

	private OAuth2TokenValidator<Jwt> buildValidator() {
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
		if (audience.isBlank()) {
			return withIssuer;
		}
		OAuth2Error audienceError = new OAuth2Error("invalid_token", "Invalid audience", null);
		OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(audience, audienceError);
		return new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);
	}
}

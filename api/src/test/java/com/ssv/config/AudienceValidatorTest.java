package com.ssv.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

	private static final String VALID_AUDIENCE = "https://api.ssvfinancial.com";
	private static final OAuth2Error AUDIENCE_ERROR = new OAuth2Error("invalid_token", "Invalid audience", null);
	private final AudienceValidator validator = new AudienceValidator(VALID_AUDIENCE, AUDIENCE_ERROR);

	@Test
	void whenAudienceMatches_thenSuccess() {
		Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").claim("aud", List.of(VALID_AUDIENCE))
				.expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now()).build();

		OAuth2TokenValidatorResult result = validator.validate(jwt);

		assertFalse(result.hasErrors());
	}

	@Test
	void whenAudienceDoesNotMatch_thenFailure() {
		Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").claim("aud", List.of("https://other-api.com"))
				.expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now()).build();

		OAuth2TokenValidatorResult result = validator.validate(jwt);

		assertTrue(result.hasErrors());
	}

	@Test
	void whenAudienceIsEmpty_thenFailure() {
		Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").claim("aud", List.of())
				.expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now()).build();

		OAuth2TokenValidatorResult result = validator.validate(jwt);

		assertTrue(result.hasErrors());
	}
}

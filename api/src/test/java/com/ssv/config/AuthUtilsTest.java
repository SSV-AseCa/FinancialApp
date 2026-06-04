package com.ssv.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthUtilsTest {

	@Test
	void extractsSubFromToken() {
		Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").claim("sub", "auth0|64f3a1b2c3d4e5f6")
				.expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now()).build();
		JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);

		assertEquals("auth0|64f3a1b2c3d4e5f6", AuthUtils.extractSub(token));
	}
}

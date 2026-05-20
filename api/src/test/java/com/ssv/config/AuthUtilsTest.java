package com.ssv.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthUtilsTest {

	@Test
	void extractsSubFromToken() {
		JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
		when(token.getName()).thenReturn("auth0|64f3a1b2c3d4e5f6");
		assertEquals("auth0|64f3a1b2c3d4e5f6", AuthUtils.extractSub(token));
	}
}

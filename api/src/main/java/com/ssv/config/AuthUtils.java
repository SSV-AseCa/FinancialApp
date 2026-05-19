package com.ssv.config;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class AuthUtils {

	private AuthUtils() {
	}

	public static String extractSub(JwtAuthenticationToken token) {
		return token.getName();
	}
}

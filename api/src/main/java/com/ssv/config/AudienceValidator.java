package com.ssv.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

	private final String audience;
	private final OAuth2Error error;

	AudienceValidator(String audience, OAuth2Error error) {
		this.audience = audience;
		this.error = error;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		if (jwt.getAudience().contains(audience)) {
			return OAuth2TokenValidatorResult.success();
		}
		return OAuth2TokenValidatorResult.failure(error);
	}
}

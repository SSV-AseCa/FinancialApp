package com.ssv;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Import({
		TestcontainersConfiguration.class,
		SsvApplicationTests.MockJwtConfig.class
})
@SpringBootTest(properties = {
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com",
		"spring.main.allow-bean-definition-overriding=true"
})
class SsvApplicationTests {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	@Test
	void contextLoads() {
	}
}
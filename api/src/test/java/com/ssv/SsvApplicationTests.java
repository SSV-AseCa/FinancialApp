package com.ssv;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Import({TestcontainersConfiguration.class, SsvApplicationTests.SecurityTestConfig.class})
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true",
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class SsvApplicationTests {

	@TestConfiguration
	static class SecurityTestConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	@Test
	void contextLoads() {
	}

}
package com.ssv.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ssv.TestcontainersConfiguration;

@Import({TestcontainersConfiguration.class, SecurityConfigTest.MockJwtConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"})
class SecurityConfigTest {

	@TestConfiguration
	static class MockJwtConfig {

		@Bean
		public JwtDecoder jwtDecoder() {
			return token -> {
				throw new UnsupportedOperationException("Mock decoder");
			};
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthEndpointIsPublic() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}

	@Test
	void protectedEndpointRequiresAuth() throws Exception {
		mockMvc.perform(get("/any-endpoint")).andExpect(status().isUnauthorized());
	}

	@Test
	void securityConfigIsNotNull(@Autowired SecurityConfig config) {
		assertNotNull(config);
	}
}

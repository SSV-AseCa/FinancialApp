package com.ssv;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
		"edgar.api.key=test-edgar-key",
		"yahoo.finance.api.key=test-yahoo-key",
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
		"auth0.audience=https://api.test.com"
})
class SsvApplicationTests {

	@Test
	void contextLoads() {
	}
}

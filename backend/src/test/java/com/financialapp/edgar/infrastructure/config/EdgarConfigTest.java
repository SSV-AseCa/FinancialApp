package com.financialapp.edgar.infrastructure.config;

import com.financialapp.edgar.application.EdgarClient;
import com.financialapp.edgar.infrastructure.client.RateLimitedEdgarClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EdgarConfigTest {

    @Test
    void edgarClientShouldCreateRateLimitedClient() {
        EdgarProperties properties = new EdgarProperties(
                "https://data.sec.gov",
                "test-agent",
                "/submissions/CIK%s.json",
                new EdgarProperties.RateLimit(10, 1000)
        );

        EdgarConfig config = new EdgarConfig();

        EdgarClient client = config.edgarClient(properties);

        assertNotNull(client);
        assertInstanceOf(RateLimitedEdgarClient.class, client);
    }
}
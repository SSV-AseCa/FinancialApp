package com.financialapp.edgar.infrastructure.ratelimit;

import com.financialapp.company.application.EdgarClient.FakeEdgarClient;
import com.financialapp.edgar.infrastructure.client.RateLimitedEdgarClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitedEdgarClientTest {

    @Test
    void getShouldAcquireRateLimitAndDelegateRequest() {
        FakeEdgarClient delegate =
                new FakeEdgarClient("company-data");

        FakeRateLimiter rateLimiter =
                new FakeRateLimiter();

        RateLimitedEdgarClient client =
                new RateLimitedEdgarClient(delegate, rateLimiter);

        String response = client.get("/submissions");

        assertEquals("company-data", response);
        assertEquals("/submissions", delegate.receivedPath());
        assertEquals(1, rateLimiter.acquireCalls());
    }
}
-- Generic, time-limited response cache shared by external-data clients (EDGAR,
-- Yahoo Finance). A read-through cache decorator stores the raw queried payload
-- keyed by (provider, cache_key) and only re-queries the upstream once an entry
-- is past expires_at. Persisted in the DB so the cache survives restarts and is
-- shared across instances.
CREATE TABLE cached_response (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(20) NOT NULL,
    cache_key VARCHAR(512) NOT NULL,
    payload TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cached_response_provider_key UNIQUE (provider, cache_key)
);

CREATE INDEX idx_cached_response_expires_at ON cached_response (expires_at);

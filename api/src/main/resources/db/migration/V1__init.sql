CREATE TABLE investor (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          auth0_sub VARCHAR(255) NOT NULL UNIQUE,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE portfolio (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           investor_id UUID NOT NULL UNIQUE REFERENCES investor(id),
                           name VARCHAR(255) NOT NULL DEFAULT 'My Portfolio',
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE market_prices (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               symbol VARCHAR(10) NOT NULL,

                               price NUMERIC(19,4) NOT NULL,

                               currency VARCHAR(10) NOT NULL,

                               fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,

                               source VARCHAR(30) NOT NULL,

                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_market_price_symbol
    ON market_prices(symbol);

CREATE INDEX idx_market_price_fetched_at
    ON market_prices(fetched_at);

CREATE INDEX idx_market_price_symbol_fetched_at
    ON market_prices(symbol, fetched_at DESC);

ALTER TABLE market_prices
    ADD CONSTRAINT uq_market_price_unique
        UNIQUE(symbol, fetched_at, source);
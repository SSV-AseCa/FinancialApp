CREATE TABLE portfolio_positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolio(id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_positions_portfolio_id
    ON portfolio_positions(portfolio_id);

CREATE INDEX idx_portfolio_positions_symbol
    ON portfolio_positions(symbol);

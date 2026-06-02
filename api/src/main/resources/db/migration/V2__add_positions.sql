CREATE TABLE position (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolio(id),
    ticker VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL,
    operation_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_position_portfolio_id ON position(portfolio_id);

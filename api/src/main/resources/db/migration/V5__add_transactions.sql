CREATE TABLE transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolio(id),
    cik VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL,
    type VARCHAR(10) NOT NULL,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transaction_portfolio_id ON transaction(portfolio_id);

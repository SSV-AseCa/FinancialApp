CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cik VARCHAR(10) NOT NULL UNIQUE,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    financials_fetched_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE financial_statements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    metric VARCHAR(80) NOT NULL,
    value NUMERIC(19,4) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    period_end VARCHAR(20),
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE sec_filings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    form_type VARCHAR(20) NOT NULL,
    filing_date VARCHAR(20) NOT NULL,
    url VARCHAR(500) NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_financial_statements_company_id
    ON financial_statements(company_id);

CREATE INDEX idx_sec_filings_company_id
    ON sec_filings(company_id);

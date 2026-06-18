-- EDGAR us-gaap tag names regularly exceed the original VARCHAR(80) limit
-- (observed up to 140 chars), causing inserts to fail when refreshing a
-- company's financial statements. TEXT removes the cap with no cost in
-- Postgres, where TEXT and VARCHAR(n) share the same storage and performance.
ALTER TABLE financial_statements
    ALTER COLUMN metric TYPE TEXT;

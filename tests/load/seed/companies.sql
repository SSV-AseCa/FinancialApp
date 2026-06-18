-- =============================================================================
-- Load-test seed: companies
-- -----------------------------------------------------------------------------
-- The `companies` table (V3__company_financial_data.sql) ships EMPTY -- it is
-- normally populated lazily via the EDGAR research/metrics path. The load-test
-- scenarios for POST /watchlist and GET /companies/{cik}/metrics need rows to
-- exist up front, so we insert a fixed, well-known set here.
--
-- This is intentionally NOT a Flyway migration: the API runs with
-- spring.jpa.hibernate.ddl-auto=validate and migrations are schema-only by
-- design. Apply this manually against the running load-test database, e.g.:
--
--   docker compose -f api/docker-compose.local.yml exec -T db \
--     psql -U ssv -d ssv < tests/load/seed/companies.sql
--
-- CIKs are stored in the 10-digit zero-padded form that WatchlistService
-- normalizes incoming CIKs to (see normalizeCik: "%010d"). Idempotent.
-- =============================================================================

INSERT INTO companies (cik, symbol, name) VALUES
	('0000320193', 'AAPL',  'Apple Inc.'),
	('0000789019', 'MSFT',  'Microsoft Corporation'),
	('0001652044', 'GOOGL', 'Alphabet Inc.'),
	('0001018724', 'AMZN',  'Amazon.com, Inc.'),
	('0001045810', 'NVDA',  'NVIDIA Corporation'),
	('0001326801', 'META',  'Meta Platforms, Inc.'),
	('0001318605', 'TSLA',  'Tesla, Inc.'),
	('0000019617', 'JPM',   'JPMorgan Chase & Co.')
ON CONFLICT (cik) DO NOTHING;

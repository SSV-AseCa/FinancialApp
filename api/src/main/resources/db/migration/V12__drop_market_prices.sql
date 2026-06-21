-- The market_prices time series is no longer used: portfolio value is read on
-- demand from the cached market data client, and cost basis is recorded at
-- purchase. Drop the table and its indexes along with the scheduler that filled
-- it.

DROP TABLE IF EXISTS market_prices;

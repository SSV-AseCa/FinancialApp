-- Cost basis is captured at purchase time rather than reconstructed from price
-- history. Each BUY records its executed unit price on the transaction event and
-- folds price x quantity into the position's running cost basis (average-cost on
-- sells). Both columns are nullable: transactions/positions created before this
-- migration have no recorded purchase price.

ALTER TABLE transaction
    ADD COLUMN price NUMERIC(19, 4);

ALTER TABLE position
    ADD COLUMN cost_basis NUMERIC(19, 4);

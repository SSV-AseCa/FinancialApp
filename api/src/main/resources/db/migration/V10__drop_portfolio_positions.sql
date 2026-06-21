-- portfolio_positions was an unused parallel holdings model; holdings live in
-- the position table. The entity and repository were removed, so drop the table
-- (its indexes are dropped with it).
DROP TABLE IF EXISTS portfolio_positions;

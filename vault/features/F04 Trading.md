---
type: feature
id: F04
priority: 4
tags: [feature]
created: 2026-05-15
---

# Trading

## Description
Covers the buy and sell flows for stocks. Operations are validated before being registered. Each confirmed transaction updates portfolio positions, portfolio balance, and the transaction history.

## Goals
- Allow investors to buy and sell shares
- Validate operations before any state is mutated
- Persist transactions and reflect them in the portfolio

## Stories
- [[Buy Shares]] — S04.02
- [[Sell Shares]] — S04.03
- [[View Transaction History]] — S04.04

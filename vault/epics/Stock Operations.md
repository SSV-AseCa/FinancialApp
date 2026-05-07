---
type: epic
tags: [epic]
created: 2026-05-06
---

# Stock Operations

## Description
Covers the buy and sell flows for stocks. Operations are validated before being registered. Each confirmed transaction updates portfolio positions, portfolio balance, and the transaction history.

## Goals
- Allow investors to buy shares of a company
- Allow investors to sell shares of a company
- Validate operations before registering them
- Persist transactions and reflect them in the portfolio

## Stories
- [[Buy Shares]]
- [[Sell Shares]]
- [[Validate Trade Operation]]
- [[View Transaction History]]

## Acceptance Criteria
- Invalid operations are rejected before any state is mutated
- A confirmed buy/sell updates: portfolio positions, portfolio balance, and transaction history
- Transaction history is accessible to the authenticated investor

## Notes

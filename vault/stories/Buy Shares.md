---
type: story
id: S04.02
feature: [[F04 Trading]]
tags: [story]
created: 2026-05-06
---

# Buy Shares

## User Story

As an investor, I want to buy shares of a company so that I can
open or increase a position.

## Acceptance Criteria

- The investor specifies the company and quantity to buy
- The system validates the operation before registering it
- On success: the transaction is stored, portfolio positions
are updated, and portfolio balance is updated
- Invalid operations are rejected before any state is mutated
- Buying operations must be validated and inform if not valid
- The buy flow is verified end-to-end on web via Cypress
- The buy flow is verified end-to-end on mobile via Appium

## Notes

---
type: story
id: S04.04
feature: [[F04 Trading]]
tags: [story]
created: 2026-05-06
---

# View Transaction History

## User Story
As an investor, I want to view my transaction history so that I can audit my past trading activity.

## Acceptance Criteria
- The authenticated investor can retrieve all their past transactions
- Each transaction record includes at minimum: type (buy/sell), company, quantity, and date
- Transactions are presented in reverse chronological order
- The transaction history view is verified end-to-end on web via Cypress
- The transaction history view is verified end-to-end on mobile via Appium

## Notes

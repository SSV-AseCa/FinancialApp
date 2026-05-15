---
type: story
id: S05.01
feature: [[F05 Portfolio Analytics]]
tags: [story]
created: 2026-05-06
---

# View Portfolio Total Value

## User Story
As an investor, I want to see the current total value of my portfolio so that I know my total market exposure.

## Acceptance Criteria
- The system calculates total portfolio value using the latest stored market prices
- The calculated value is presented to the authenticated investor
- Value is derived from stored prices, not live price calls
- The total value view is verified end-to-end on web via Cypress
- The total value view is verified end-to-end on mobile via Appium

## Notes

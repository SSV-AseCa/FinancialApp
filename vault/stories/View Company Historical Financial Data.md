---
type: story
id: S06.04
feature: [[F06 Company Research]]
tags: [story]
created: 2026-05-06
---

# View Company Historical Financial Data

## User Story
As an investor, I want to view historical financial data for a company so that I can analyze trends over time.

## Acceptance Criteria
- After selecting a company, the system retrieves historical financial data from EDGAR
- Historical data is presented in a way that allows trend analysis
- EDGAR rate limit is respected during retrieval
- The historical data view is verified end-to-end on web via Cypress
- The historical data view is verified end-to-end on mobile via Appium

## Notes

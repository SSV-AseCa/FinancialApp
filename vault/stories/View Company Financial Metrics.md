---
type: story
id: S06.02
feature: [[F06 Company Research]]
tags: [story]
created: 2026-05-06
---

# View Company Financial Metrics

## User Story
As an investor, I want to view the current financial metrics for a company so that I can assess its financial health.

## Acceptance Criteria
- After selecting a company, the system retrieves its financial metrics from EDGAR
- Metrics are presented to the investor
- EDGAR rate limit is respected during retrieval
- The financial metrics view is verified end-to-end on web via Cypress
- The financial metrics view is verified end-to-end on mobile via Appium

## Notes

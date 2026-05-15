---
type: story
id: S06.03
feature: [[F06 Company Research]]
tags: [story]
created: 2026-05-06
---

# View Company SEC Filings

## User Story
As an investor, I want to view recent SEC filings for a company so that I can read regulatory disclosures.

## Acceptance Criteria
- After selecting a company, the system retrieves its most recent SEC filings from EDGAR
- Filings are presented to the investor
- EDGAR rate limit is respected during retrieval
- The SEC filings view is verified end-to-end on web via Cypress
- The SEC filings view is verified end-to-end on mobile via Appium

## Notes

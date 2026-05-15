---
type: story
id: S01.03
feature: [[F01 Authentication]]
tags: [story]
created: 2026-05-06
---

# Log In

## User Story
As an investor, I want to log in with my credentials so that I can access my protected resources.

## Acceptance Criteria
- The investor provides valid credentials
- The system authenticates the investor and establishes a session
- Invalid credentials are rejected with an appropriate message
- An authenticated investor can access portfolio, watchlist, and trading features
- The login flow is verified end-to-end on web via Cypress
- The login flow is verified end-to-end on mobile via Appium

## Notes

---
type: story
id: S02.05
epic: [[Infrastructure and DevOps]]
tags: [story]
created: 2026-05-06
---

# E2E Tests

## User Story

As a developer, I want end-to-end tests that validate core investor workflows
on the real web and mobile apps so that the system is verified from the user's perspective.

## Acceptance Criteria

- E2E tests are implemented using Cypress (web) or Appium (mobile)
- Core investor workflows are covered: portfolio management,
stock operations, company research, watchlist
- Tests run against the deployed application, not mocks
- Tests run as part of the CI/CD pipeline

## Notes

The UI is intentionally minimal — tests should focus on
validating core workflows only.

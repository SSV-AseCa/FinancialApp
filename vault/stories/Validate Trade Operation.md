---
type: story
epic: [[Stock Operations]]
tags: [story]
created: 2026-05-06
---

# Validate Trade Operation

## User Story
As a system, I want to validate a requested trade operation before registering it so that invalid or inconsistent transactions are never persisted.

## Acceptance Criteria
- Validation runs before any state mutation
- A sell operation is rejected if the investor does not hold sufficient shares
- An operation with missing or invalid fields (company, quantity, type) is rejected
- Validation errors are communicated to the investor with a descriptive message

## Notes
This is a system-level story that underpins both [[Buy Shares]] and [[Sell Shares]].

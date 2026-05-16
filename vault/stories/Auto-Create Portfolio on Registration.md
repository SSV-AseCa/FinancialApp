---
type: story
id: S01.02
feature: [[F01 Authentication]]
tags: [story]
created: 2026-05-06
---

# Auto-Create Portfolio on Registration

## User Story

As a system, I want to automatically create an empty
portfolio when an investor registers so that the investor has a
portfolio ready to use immediately.

## Acceptance Criteria

- A portfolio is created as part of the registration transaction
- The portfolio is empty at creation
- Each investor has exactly one portfolio
- The portfolio is associated exclusively with the registering investor

## Tasks

- [[T01.02 API]]

## Notes

This is a system-initiated story triggered by the Register Account story.

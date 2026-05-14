---
type: epic
id: E03
tags: [epic]
created: 2026-05-06
---

# Portfolio Management

## Description
Allows authenticated investors to build and maintain their stock portfolio through CRUD operations on positions. Every portfolio belongs to exactly one investor and all changes are persisted.

## Goals
- Allow investors to view their current portfolio and positions
- Allow investors to add new stock positions specifying ticker, quantity, and operation date
- Allow investors to modify existing positions
- Allow investors to remove positions from the portfolio

## Stories
- [[View Portfolio]]
- [[Add Position]]
- [[Modify Position]]
- [[Remove Position]]

## Acceptance Criteria
- Only authenticated investors can access their own portfolio
- Adding a position requires ticker, quantity, and operation date
- All portfolio mutations are persisted

## Notes

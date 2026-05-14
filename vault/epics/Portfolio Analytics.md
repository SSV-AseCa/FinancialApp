---
type: epic
id: E06
tags: [epic]
created: 2026-05-06
---

# Portfolio Analytics

## Description
Provides read-only performance metrics for an investor's portfolio. Metrics are calculated using the latest stored market prices — no real-time price calls are made at query time.

## Goals
- Calculate and present the current total portfolio value
- Calculate and present profit/loss for each individual position
- Aggregate and present overall portfolio performance metrics

## Stories
- [[View Portfolio Total Value]]
- [[View Position Profit and Loss]]
- [[View Portfolio Performance Metrics]]

## Acceptance Criteria
- Metrics are derived from the latest stored market prices, not live price calls
- Balance and P&L calculations are consistent with stored position data
- Metrics are only accessible to authenticated investors

## Notes
Open question: how often and by which method should market values be stored? This directly affects the freshness of analytics data. See [[Open Questions]].

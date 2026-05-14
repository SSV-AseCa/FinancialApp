---
type: epic
id: E04
tags: [epic]
created: 2026-05-06
---

# Market Data Integration

## Description
Cross-cutting concern responsible for sourcing and storing external financial data. Market prices are fetched from Yahoo Finance. Company financials and filings are fetched from SEC EDGAR. EDGAR's public rate limit of 10 req/sec must be enforced at the integration layer.

## Goals
- Fetch and store market prices from Yahoo Finance so analytics have current data
- Enforce EDGAR rate limit to avoid being throttled or blocked
- Fetch and store company financial data and filings from EDGAR for research features

## Stories
- [[Fetch and Store Market Prices]]
- [[Enforce EDGAR Rate Limit]]
- [[Fetch and Store Company Financial Data]]

## Acceptance Criteria
- Stored prices are the source of truth for portfolio analytics
- No more than 10 EDGAR requests per second are issued at any point
- Financial data and filings are available to the Company Research and Watchlist epics

## Notes
Open question: how often and by which method should market prices be fetched and stored? See [[Open Questions]].

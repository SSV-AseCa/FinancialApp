---
type: feature
id: F03
priority: 3
tags: [feature]
created: 2026-05-15
---

# Market Data

## Description
Cross-cutting concern responsible for sourcing and storing external financial data. Market prices are fetched from Yahoo Finance. Company financials and filings are fetched from SEC EDGAR. EDGAR's rate limit of 10 req/sec must be enforced at the integration layer.

## Goals
- Fetch and store market prices so analytics have current data without live calls at query time
- Enforce the EDGAR rate limit to avoid throttling
- Fetch and store company financial data and filings for Company Research and Watchlist

## Stories
- [[Enforce EDGAR Rate Limit]] — S03.01
- [[Fetch and Store Market Prices]] — S03.02
- [[Fetch and Store Company Financial Data]] — S03.03

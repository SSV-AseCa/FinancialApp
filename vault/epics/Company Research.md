---
type: epic
tags: [epic]
created: 2026-05-06
---

# Company Research

## Description
Allows investors to discover and evaluate companies before investing. Data is sourced from the SEC EDGAR API. Covers company search, current financial metrics, recent SEC filings, and historical financial data.

## Goals
- Allow investors to search for companies by name or ticker
- Present current financial metrics for a selected company
- Present the most recent SEC filings for a selected company
- Present historical financial data for a selected company

## Stories
- [[Search Companies]]
- [[View Company Financial Metrics]]
- [[View Company SEC Filings]]
- [[View Company Historical Financial Data]]

## Acceptance Criteria
- Search queries are forwarded to SEC EDGAR API
- Financial metrics, filings, and historical data are retrieved and presented to the investor
- EDGAR rate limit (10 req/sec) is respected at all times

## Notes

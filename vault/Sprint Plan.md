---
type: sprint-plan
created: 2026-05-16
---

# Sprint Plan

## Team

| Role | Count |
|------|-------|
| API Developer | 2 |
| Frontend Core | 1 |
| Mobile + Appium | 1 |
| Web + Cypress | 1 |

Each story is a full vertical slice: backend, frontend core, web frontend + Cypress, mobile frontend + Appium. The breakdown per technology is handled at the task level inside each story.

## Timeline

- **Actual start:** 14-05-2026 (3 weeks behind original plan)
- **Presentation:** 18-06-2026
- **Available:** 5 cycles — no buffer
- **Cycle duration:** 1 week

---

## Cycle 1 — 14-05 to 20-05 | Authentication

| Story | ID |
|-------|----|
| Register Account | S01.01 |
| Auto-Create Portfolio on Registration | S01.02 |
| Log In | S01.03 |
| Log Out | S01.04 |
| Enforce EDGAR Rate Limit | S03.01 |

**Delivers:** [[F01 Authentication]]

---

## Cycle 2 — 21-05 to 27-05 | Portfolio Management + Company Search

| Story | ID |
|-------|----|
| View Portfolio | S02.01 |
| Add Position | S02.02 |
| Modify Position | S02.03 |
| Remove Position | S02.04 |
| Search Companies | S06.01 |

**Delivers:** [[F02 Portfolio Management]], [[F06 Company Research]]

---

## Cycle 3 — 28-05 to 03-06 | Trading + Market Data Pipeline

| Story | ID |
|-------|----|
| Buy Shares | S04.02 |
| Sell Shares | S04.03 |
| View Transaction History | S04.04 |
| Fetch and Store Company Financial Data | S03.03 |
| Fetch and Store Market Prices | S03.02 |

**Delivers:** [[F03 Market Data]], [[F04 Trading]]

> Buy and Sell are the heaviest stories in the project.

---

## Cycle 4 — 04-06 to 10-06 | Portfolio Analytics + Company Research

| Story | ID |
|-------|----|
| View Portfolio Total Value | S05.01 |
| View Position Profit and Loss | S05.02 |
| View Portfolio Performance Metrics | S05.03 |
| View Company Financial Metrics | S06.02 |
| View Company SEC Filings | S06.03 |

**Delivers:** [[F05 Portfolio Analytics]]

---

## Cycle 5 — 11-06 to 17-06 | Watchlist + Integration + Polish

| Story | ID |
|-------|----|
| View Company Historical Financial Data | S06.04 |
| Add Company to Watchlist | S07.01 |
| Remove Company from Watchlist | S07.02 |
| View Watchlist Financial Metrics | S07.03 |
| Compare Watchlist Companies | S07.04 |

**Delivers:** [[F07 Watchlist]]

> This cycle has zero buffer before the 18-06 presentation. Spillover from cycle 4 is a critical risk.

---

## Risk

The 3-week late start compresses an 8-week project into 5 cycles. The pace (5 stories/cycle) is achievable only if:
1. No story carries over to the next cycle
2. Cycle 5 is treated as delivery — no new scope

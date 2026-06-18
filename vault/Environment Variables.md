---
type: operations
created: 2026-05-19
updated: 2026-05-19
---

# Environment Variables

This document covers two scopes:

- **GitHub Actions** — secrets and variables that must be configured in the repository settings for CI/CD to work
- **Local development** — per-module variables needed to run each service locally; see the `.env.sample` file in each module directory

---

## GitHub Actions — Secrets

Configure at: **Settings → Secrets and variables → Actions → Secrets**

| Secret | Required by | Description |
|--------|-------------|-------------|
| `SONAR_TOKEN` | `api-sonar` job | SonarCloud authentication token. Generate at sonarcloud.io → Account → Security |
| `EDGAR_API_KEY` | `api-test` job | SEC EDGAR API key for integration tests that hit the real endpoint |
| `YAHOO_FINANCE_API_KEY` | `api-test` job | Yahoo Finance API key for integration tests that hit the real endpoint |

`GITHUB_TOKEN` is provided automatically by GitHub Actions and does not need to be configured.

---

## GitHub Actions — Variables

Configure at: **Settings → Secrets and variables → Actions → Variables**

| Variable | Value | Description |
|----------|-------|-------------|
| `SONARCLOUD_ENABLED` | `true` | Enables the `api-sonar` job. Set this only after `SONAR_TOKEN` is also configured and the SonarCloud project exists. Leave unset or set to any other value to keep the job skipped. |

---

## Local Development — Per-module

Each module has a `.env.sample` at its root listing every variable the service reads at runtime. Copy it to `.env` and fill in real values before starting the service locally.

`.env` files are git-ignored. `.env.sample` files are tracked and must never contain real credentials.

| Module | Sample file | Notes |
|--------|-------------|-------|
| `api/` | `api/.env.sample` | Spring Boot datasource + external API keys |
| `apps/web/` | `apps/web/.env.sample` | Vite public vars exposed to the browser |
| `apps/mobile/` | `apps/mobile/.env.sample` | Vite public vars for the Capacitor shell |
| `apps/ui-core/` | `apps/ui-core/.env.sample` | Build-time vars for the component library |

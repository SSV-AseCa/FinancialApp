# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SSV** is a USA stock portfolio tracking application with a Java/Spring Boot backend, a shared React component library (`ui-core`), a React web app, and a Capacitor-based Android mobile app. It integrates SEC EDGAR (company filings) and Yahoo Finance (market prices).

---

## Monorepo Layout

```
api/                  Spring Boot REST API (Java 21, Gradle)
apps/ui-core/         Shared React component library — owns the API client
apps/web/             Web frontend (React + Vite); Cypress E2E in cypress/
apps/mobile/          Mobile app (React + Capacitor/Android); Appium E2E in appium/
tests/load/           Locust load/stress scenarios (run on demand, not in CI)
vault/                Project knowledge base (Obsidian)
```

**Dependency rule:** `apps/web` and `apps/mobile` both depend on `apps/ui-core`. Neither app calls the API directly — `ui-core` owns the API client. `api/` has no knowledge of the frontend.

---

## Commands

### API (`api/`)

```bash
# Run unit + integration tests (Testcontainers spins up PostgreSQL automatically)
cd api && ./gradlew test

# Run a single test class
cd api && ./gradlew test --tests "com.ssv.SomeTest"

# Full static analysis + coverage check (same as CI)
cd api && ./gradlew spotlessCheck checkstyleMain pmdMain spotbugsMain jacocoTestReport jacocoTestCoverageVerification

# Auto-fix formatting
cd api && ./gradlew spotlessApply

# Build the JAR
cd api && ./gradlew build
```

JaCoCo enforces **75% minimum line coverage**. `SsvApplication.class` is excluded from coverage calculations.

Static analysis tools: Spotless (Eclipse formatter), Checkstyle (`config/checkstyle/`), PMD (`config/pmd/`), SpotBugs.

### Frontend — all JS packages (run from apps/)

Install: `pnpm install` (always from `apps/`; pnpm-workspace.yaml there includes `ui-core`, `web`, and `mobile`).

```bash
# ui-core
cd apps/ui-core
pnpm lint          # ESLint
pnpm typecheck     # tsc -b
pnpm test -- --run # Vitest (single run)
pnpm build         # Produces dist/ consumed by web and mobile Dockerfiles

# apps/web
cd apps/web
pnpm lint
pnpm typecheck
pnpm test -- --run
pnpm dev           # Dev server
pnpm test:e2e      # Cypress (requires running stack)

# apps/mobile
cd apps/mobile
pnpm lint
pnpm typecheck
pnpm test -- --run
pnpm dev
pnpm test:e2e      # Appium via wdio.conf.ts (requires emulator + Appium server)
```

### Mobile APK build sequence

```bash
cd apps/mobile
pnpm build                          # Vite → dist/
npx cap sync android                # Copy web assets into android/
cd android && ./gradlew assembleDebug
```

### Run full stack locally

```bash
docker compose up   # from repo root — starts API + PostgreSQL + web + mobile
```

Each module also has its own `docker-compose.yml` for isolated dev. The compose files support image override via env vars (`API_IMAGE`, `WEB_IMAGE`, `MOBILE_IMAGE`) for CI injection — always preserve this pattern.

---

## Local Environment Setup

Each module has a `.env.sample`. Copy it to `.env` and fill in values:

| Module | Key variables |
|--------|--------------|
| `api/` | `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `EDGAR_API_KEY`, `YAHOO_FINANCE_API_KEY` |
| `apps/web/` | `VITE_API_BASE_URL` (default `http://localhost:8080`) |
| `apps/mobile/` | same as web |
| `apps/ui-core/` | none required |

Tests in `api/` use Testcontainers and do **not** need `SPRING_DATASOURCE_*`. `EDGAR_API_KEY` and `YAHOO_FINANCE_API_KEY` are needed for integration tests that hit real endpoints.

---

## Architecture

### API (`api/`) — Onion / layered Spring Boot

Currently scaffolded only (`SsvApplication.java`). Domain model maps to the stories in `vault/stories/`. Key domain rules:

- One investor → exactly one portfolio (auto-created on registration)
- Authentication required for all portfolio features
- External integrations: EDGAR (SEC filings) and Yahoo Finance (market prices)

Database migrations use **Flyway**. Schema changes go in `src/main/resources/db/migration/`.

### `apps/ui-core/` — Shared component library

Single entry point at `src/index.ts`. Built to `dist/` via Vite library mode. Web and mobile Dockerfiles copy from this `dist/` — run `pnpm build` here before building those images.

### `apps/web/` and `apps/mobile/`

Both consume `@ssv/ui-core` (workspace dependency). During development, stubs matching the `ui-core` interface are used so work can proceed before `ui-core` is complete. At integration time stubs are replaced with real imports without changing screen code.

---

## CI/CD Pipeline

Chain on merge to `main`: `ci.yaml` → `e2e.yaml` → `release.yaml`. PRs only run `ci.yaml` and `pr-title.yaml`.

**PR titles** must follow Conventional Commits with valid scopes: `api`, `common`, `web`, `mobile`. For `feat` and `fix`, scope is required or semantic-release will not produce a release.

**Releases** are per-module and triggered only after E2E passes. Tag format: `api-v*`, `common-v*`, `web-v*`, `mobile-v*`. The `.released-version` sentinel file in each module must never be committed (it's git-ignored).

**SonarCloud** is the quality gate for `api/`. It is controlled by the `SONARCLOUD_ENABLED` repo variable — do not enable it until `SONAR_TOKEN` is configured and the project exists on sonarcloud.io.

Composite actions live in `.github/actions/`. Add repeated step sequences there rather than inlining them in workflow jobs.

---

## Quality Gates

| Gate | Threshold | Enforced by |
|------|-----------|-------------|
| API line coverage | 75% minimum | JaCoCo (`jacocoTestCoverageVerification`) |
| API static analysis | zero violations | Checkstyle, PMD, SpotBugs, Spotless |
| API quality gate | pass | SonarCloud (`api-sonar` job) |

Load/stress tests (Locust, `tests/load/`) are not automated — results must be documented before final presentation (2026-06-18).

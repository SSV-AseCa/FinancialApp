---
type: architecture
created: 2026-05-18
updated: 2026-05-18
---

# Tech Stack

Per-module technology decisions. Sections marked **[implemented]** reflect confirmed choices derived from source files. Sections marked **[planned]** reflect design decisions not yet scaffolded.

See [[Monorepo Structure]] for module layout and [[CI]] for how these are exercised in the pipeline.

---

## `api/` — Backend REST API [implemented as `backend/`]

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 (toolchain) |
| Framework | Spring Boot | 3.5.14 |
| Build tool | Gradle | (wrapper in repo) |
| Web layer | Spring Web (MVC) | via Boot |
| Security | Spring Security | via Boot |
| Persistence | Spring Data JPA | via Boot |
| Database | PostgreSQL | (runtime) |
| Migrations | Flyway | (PostgreSQL dialect) |
| Observability | Spring Actuator | via Boot |
| Validation | Spring Validation (Bean Validation) | via Boot |
| Boilerplate reduction | Lombok | (compile + annotation processor) |
| Testing framework | JUnit 5 | via `junit-platform-launcher` |
| Integration testing | Testcontainers (PostgreSQL) | via Boot testcontainers |
| Security testing | Spring Security Test | via Boot |
| Coverage | JaCoCo | (minimum 75% line coverage enforced) |
| Static analysis | Checkstyle | via Gradle plugin |
| Static analysis | PMD | via Gradle plugin |
| Static analysis | SpotBugs | 6.0.26 |
| Code formatting | Spotless (Eclipse formatter) | 7.0.2 |
| Quality gate | SonarCloud | org.sonarqube 7.3.0.8198 |

External API integrations: **EDGAR** (SEC filings) and **Yahoo Finance** (market prices). API keys are injected via secrets at test time.

---

## `packages/ui-core/` — Shared UI Library [planned]

| Layer | Technology |
|-------|-----------|
| Language | TypeScript |
| UI library | React |
| Bundler | Vite |
| Package manager | pnpm |
| Linting | ESLint |
| Unit testing | Vitest |

Consumed by both `apps/web/` and `apps/mobile/`. Owns the API client — neither app calls the API directly. Built to `dist/` as an artifact consumed by the web and mobile Docker build contexts.

---

## `apps/web/` — Web Application [planned]

| Layer | Technology |
|-------|-----------|
| Language | TypeScript |
| UI library | React |
| Bundler | Vite |
| Package manager | pnpm |
| Linting | ESLint |
| Unit testing | Vitest |
| E2E testing | Cypress |

Cypress tests live in `apps/web/cypress/` (co-located). E2E runs against the full Docker stack.

---

## `apps/mobile/` — Mobile Application [planned]

| Layer | Technology |
|-------|-----------|
| Language | TypeScript |
| UI library | React |
| Bundler | Vite |
| Mobile wrapper | Capacitor (Android target) |
| Package manager | pnpm |
| Linting | ESLint |
| Unit testing | Vitest |
| E2E testing | Appium + uiautomator2 driver |

Capacitor wraps the Vite-built web assets into a native Android shell. The build sequence is: `pnpm build` → `cap sync android` → Gradle `assembleDebug`. Appium tests run against an Android emulator Docker container (no host-level emulator).

---

## `tests/load/` — Load and Stress Testing [planned]

| Tool | Purpose |
|------|---------|
| Locust | Load and stress test scenarios against the deployed system |

At minimum two scenarios required: normal load and peak/stress. Results must be documented before the final presentation. See [[Delivery and Quality Constraints]].

---

## Infrastructure

| Concern | Technology |
|---------|-----------|
| Containerisation | Docker |
| Local orchestration | Docker Compose (per-module `docker-compose.yml` files) |
| Image registry | GitHub Container Registry (GHCR) |
| Primary database | PostgreSQL |

Each deployable module (`api/`, `apps/web/`, `apps/mobile/`) has its own `Dockerfile` and `docker-compose.yml`. The root `docker-compose.yml` composes the full system. Compose files must support image override via env vars (`${API_IMAGE:-ssv-api:latest}`) to allow CI-built images to be injected at E2E time.

---

## CI/CD

| Concern | Technology |
|---------|-----------|
| Pipeline platform | GitHub Actions |
| Composite action abstractions | 6 local composite actions (see [[CI]]) |
| Versioning strategy | Semantic Versioning via `semantic-release` |
| Commit convention | Conventional Commits (enforced on PR titles) |
| Per-module release routing | Commit scopes (`api`, `common`, `web`, `mobile`) |
| Static analysis gateway | SonarCloud |
| Coverage gateway | JaCoCo (75% minimum, verified in CI) |

---
type: ci-design
created: 2026-05-18
updated: 2026-05-18
---

# CI Pipeline Design

This document describes the complete, implemented CI/CD pipeline. It covers four workflow files, the per-module Docker image strategy, the E2E architecture, and the release process. The source of truth for implementation is [[Monorepo Structure]] and [[Delivery and Quality Constraints]].

---

## Workflow Files

| File | Trigger | Responsibility |
|------|---------|----------------|
| `.github/workflows/pr-title.yaml` | `pull_request` (opened, edited, synchronize, reopened) | Validates PR titles against the Conventional Commits spec |
| `.github/workflows/ci.yaml` | Every push, every pull request | Phase 1 (lint + unit tests) and Phase 2 (builds + SonarCloud) |
| `.github/workflows/e2e.yaml` | `workflow_run` on CI completing on `main` | Phase 3+4 (Docker stack + E2E) and GHCR cleanup |
| `.github/workflows/release.yaml` | `workflow_run` on E2E completing on `main` | Per-module semantic versioning and permanent Docker image publishing |

The chain on a merge to `main` is:

```
push to main
  → ci.yaml         (Phase 1+2, always)
    → e2e.yaml      (Phase 3+4, only if CI passed)
      → release.yaml (release, only if E2E passed)
```

On pull requests only `ci.yaml` and `pr-title.yaml` run. E2E and release are exclusive to `main`.

---

## PR Title Enforcement

Every PR title must conform to the Conventional Commits specification. The `amannn/action-semantic-pull-request` action validates this on every relevant pull request event, including `edited`, so correcting a bad title re-runs the check without requiring a new commit.

**Valid types:** `feat`, `fix`, `chore`, `docs`, `ci`, `refactor`, `test`, `perf`, `style`

**Valid scopes:** `api`, `common`, `web`, `mobile`

Scope is optional for `chore`, `docs`, `ci`, and similar non-release types. For `feat` and `fix`, a scope must be provided or semantic-release will not know which module to release. A `feat:` without a scope is syntactically valid but produces no release — this is a silent failure that code review must catch.

---

## Phase 1 — Static Analysis and Unit Tests

All Phase 1 jobs run in parallel. No module waits for another.

### `api/`

- **Lint:** Spotless (format), Checkstyle, PMD, SpotBugs via `./gradlew spotlessCheck checkstyleMain pmdMain spotbugsMain`
- **Test:** JUnit + Testcontainers (PostgreSQL spun up automatically) via `./gradlew test jacocoTestReport jacocoTestCoverageVerification`. External API secrets (`EDGAR_API_KEY`, `YAHOO_FINANCE_API_KEY`) are injected for integration tests. JaCoCo report is uploaded as an artifact for the SonarCloud job.

### `packages/ui-core/`

- **Lint:** ESLint + TypeScript compiler check (`pnpm lint`, `pnpm typecheck`)
- **Test:** Vitest (`pnpm test -- --run`)

### `apps/web/`

- **Lint:** ESLint + TypeScript compiler check
- **Test:** Vitest with local `ui-core` stubs

### `apps/mobile/`

- **Lint:** ESLint + TypeScript compiler check
- **Test:** Vitest with local `ui-core` stubs

---

## Phase 2 — Builds and SonarCloud

All Phase 2 jobs run in parallel. Each depends on its own Phase 1 jobs passing.

### SonarCloud (`api-sonar`)

Runs after `api-lint` and `api-test` pass. Downloads the JaCoCo artifact from `api-test` and runs `./gradlew sonar -x test`. This is a blocking gate: E2E will not start if Sonar fails.

### `ui-core-build`

Runs `pnpm build` and uploads the compiled `dist/` as an artifact consumed by `web-build` and `mobile-build`.

### `api-build`, `web-build`, `mobile-build`

Each module has its own `Dockerfile` and `docker-compose.yml`. The build jobs run `docker/build-push-action`:

- On **pull requests**: the image is built (Dockerfile health is verified) but **not pushed**.
- On **push to main**: the image is built **and pushed** to GHCR tagged with the commit SHA (`ssv-api:<sha>`, `ssv-web:<sha>`, `ssv-mobile:<sha>`). These SHA-tagged images are temporary — they exist only for the E2E job and are deleted by the cleanup step.

`web-build` and `mobile-build` declare `needs: [ui-core-build]` and download the `ui-core-dist` artifact before running Docker build, because their Dockerfiles copy from `packages/ui-core/dist/`.

---

## Phase 3+4 — E2E Tests (`e2e.yaml`)

Triggered by `workflow_run` on `ci.yaml` completing on `main`. Only proceeds if `conclusion == 'success'`.

### Image sharing between workflows

GitHub Actions runners are ephemeral — a Docker image built in one job does not survive into another job or another workflow. The SHA-tagged GHCR images pushed in Phase 2 are how `e2e.yaml` receives the verified images from `ci.yaml`.

Each E2E job:
1. Logs in to GHCR with `GITHUB_TOKEN`
2. Starts the Docker stack by injecting the GHCR image references as environment variables (`API_IMAGE`, `WEB_IMAGE`, `MOBILE_IMAGE`) and running `docker compose up -d --no-build --wait`
3. Runs the test suite
4. The compose files must support image override via these env vars (e.g. `image: ${API_IMAGE:-ssv-api:latest}`)

### Cypress (web)

Uses the root `docker-compose.yml` to start the full stack (API + database + web frontend). Points Cypress at `http://localhost:3000`. Screenshots are uploaded as artifacts on failure.

### Appium (mobile)

Uses `api/docker-compose.yml` combined with `apps/mobile/docker-compose.yml` to start the API stack and the Android emulator container. Appium on the runner connects to the emulator container via its exposed port. No host-level Android emulator is needed.

### Cleanup

After both E2E jobs complete (whether they pass or fail), a cleanup job deletes the three SHA-tagged images from GHCR using `actions/delete-package-versions`. This runs with `if: always()` so it executes even if E2E fails, preventing transient images from accumulating. `continue-on-error: true` on each delete step guards against the edge case where a build failed before pushing and left nothing to delete.

---

## Load and Stress Tests

Locust scenarios in `tests/load/` are not part of any automated pipeline. They run against the full Docker-deployed system on demand. Results must be documented before final delivery per [[Delivery and Quality Constraints]].

---

## Release Pipeline (`release.yaml`)

Triggered by `workflow_run` on `e2e.yaml` completing on `main`. Only proceeds if `conclusion == 'success'`. This guarantees a release is only cut from a commit that passed the complete quality chain.

### Commit convention and scope routing

Releases use `semantic-release`. Each module has its own `.releaserc.json` that only responds to commits scoped to that module:

| Commit | Effect |
|--------|--------|
| `feat(api): add portfolio endpoint` | `api` minor bump |
| `fix(web): correct chart colours` | `web` patch bump |
| `feat!(api): redesign auth contract` | `api` major bump |
| `chore: update dependencies` | no release (no scope) |
| `feat(common): new input component` | `common` minor bump |

Commits without a matching scope are invisible to all modules. If no scoped commits exist since the last tag for a module, semantic-release exits 0 ("no release") and the next module proceeds.

### Tag format per module

| Module | Tag format | Example |
|--------|-----------|---------|
| `api/` | `api-v${version}` | `api-v1.3.0` |
| `packages/ui-core/` | `common-v${version}` | `common-v2.0.1` |
| `apps/web/` | `web-v${version}` | `web-v1.1.0` |
| `apps/mobile/` | `mobile-v${version}` | `mobile-v1.1.0` |

### Execution order

The four release jobs run **sequentially**: `api → common → web → mobile`. This is required because `@semantic-release/git` commits a changelog and updated version file back to `main` with `[skip ci]` for each module that releases. Running in parallel would cause concurrent non-fast-forward push failures.

If a module has no commits to release, semantic-release exits without touching git and the next job starts immediately.

### What each release produces

**`api`**
- GitHub Release tagged `api-v<version>` with generated changelog
- `build.gradle` version updated and committed back to `main`
- Permanent GHCR image `ssv-api:<version>` (not deleted, unlike CI images)

**`common`**
- GitHub Release tagged `common-v<version>` with generated changelog
- `package.json` version updated and committed back to `main`
- No Docker image (shared library, not a deployable service)

**`web`**
- GitHub Release tagged `web-v<version>` with generated changelog
- `package.json` version updated and committed back to `main`
- Permanent GHCR image `ssv-web:<version>`

**`mobile`**
- GitHub Release tagged `mobile-v<version>` with generated changelog
- `package.json` version updated and committed back to `main`
- Debug APK (`app-debug.apk`) attached as a release asset (built fresh from source during release job)
- Permanent GHCR image `ssv-mobile:<version>`

### Version sentinel file

`@semantic-release/exec`'s `publishCmd` writes the released version to `.released-version` in each module directory. The subsequent Docker build step reads this file to tag the image. If the file does not exist, no release happened and the Docker build is skipped. `.released-version` is listed in `.gitignore` and must never be committed.

---

## Composite Actions

All repeated step sequences are extracted into composite actions under `.github/actions/`. Each action encapsulates exactly one concern (Single Responsibility) and is reused across the three main workflow files (DRY).

| Action | Wraps | Used by |
|--------|-------|---------|
| `setup-java-gradle` | `actions/setup-java@v4` (Java 21, Temurin) + `gradle/actions/setup-gradle@v4` | `api-lint`, `api-test`, `api-sonar`, `api-build` in CI; `release-api`, `release-mobile` in Release |
| `setup-pnpm` | `pnpm/action-setup@v4` + `actions/setup-node@v4` (Node 20); accepts optional `cache-dependency-path` | All frontend lint/test/build jobs in CI; `cypress`, `appium` in E2E; all release jobs |
| `docker-setup` | `docker/setup-buildx-action@v3` + `docker/login-action@v3` (GHCR) | `api-build`, `web-build`, `mobile-build` in CI; `cypress`, `appium` in E2E |
| `git-identity` | `git config` for `github-actions[bot]` identity | All four release jobs |
| `install-semantic-release` | `pnpm add -g` for the base semantic-release plugin set; accepts `extra-plugins` for module-specific additions | All four release jobs |
| `publish-docker` | Reads `.released-version` sentinel; conditionally runs buildx + GHCR login + `docker/build-push-action@v6` | `release-api`, `release-web`, `release-mobile` |

The `publish-docker` action is self-contained: it performs its own Docker setup internally so release jobs need no separate `docker-setup` step.

---

## Secrets Required

| Secret | Used by | Purpose |
|--------|---------|---------|
| `GITHUB_TOKEN` | all workflows | checkout, GHCR push/pull/delete, GitHub releases |
| `SONAR_TOKEN` | `api-sonar` | SonarCloud analysis authentication |
| `EDGAR_API_KEY` | `api-test` | Real EDGAR endpoint for integration tests |
| `YAHOO_FINANCE_API_KEY` | `api-test` | Real Yahoo Finance endpoint for integration tests |

`GITHUB_TOKEN` is provided automatically by GitHub Actions. The others must be configured as repository secrets.

---

## Compose File Contract

Each module's `docker-compose.yml` must support image override via environment variables so E2E jobs can inject GHCR images without modifying the file:

```yaml
services:
  api:
    image: ${API_IMAGE:-ssv-api:latest}
    build: .
```

The `build:` key remains for local development. In CI, `--no-build` is passed and the env var provides the pre-built image.

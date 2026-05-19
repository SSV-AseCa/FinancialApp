# Release Checklist

How to trigger a semantic-versioned release. See [[CI]] for the full pipeline architecture.

---

## 1. GitHub Secrets

Configure under **Settings → Secrets and variables → Actions → Secrets**.

| Secret | Required by | Notes |
|---|---|---|
| `GITHUB_TOKEN` | All workflows | Auto-provided by GitHub — no action needed |
| `EDGAR_API_KEY` | `api-test` | Spring Boot integration tests |
| `YAHOO_FINANCE_API_KEY` | `api-test` | Spring Boot integration tests |
| `SONAR_TOKEN` | `api-sonar` | Only needed if SonarCloud is enabled |

## 2. GitHub Variables

Configure under **Settings → Secrets and variables → Actions → Variables**.

| Variable | Value | Effect if absent |
|---|---|---|
| `SONARCLOUD_ENABLED` | `true` | `api-sonar` job is silently skipped |

## 3. Commit Convention

Releases are driven entirely by commit message scopes. semantic-release scans commits since the last tag for each module and only cuts a release when it finds a matching one.

### Scopes

| Scope | Module |
|---|---|
| `api` | Spring Boot backend |
| `common` | `packages/ui-core` shared library |
| `web` | React web app |
| `mobile` | Capacitor Android app |

### Bump Rules

| Commit | Version bump |
|---|---|
| `feat(scope): …` | minor (x.**Y**.0) |
| `fix(scope): …` | patch (x.y.**Z**) |
| `perf(scope): …` | patch |
| `refactor(scope): …` | patch |
| Any commit with `BREAKING CHANGE` footer or `!` | major (**X**.0.0) |
| `chore:`, `docs:`, `ci:`, `test:` (any scope) | no release |
| Missing or unknown scope | invisible to all modules — no release |

### Examples

```
feat(api): add stock screener endpoint         → api  minor
fix(web): correct portfolio chart scaling      → web  patch
refactor(common): simplify Button props        → common patch
feat(mobile)!: redesign bottom navigation      → mobile MAJOR
chore: update dependencies                     → nothing released
```

> **Silent failure to watch for:** `feat:` without a scope is syntactically valid Conventional Commits but produces no release. Code review should reject unscoped `feat` and `fix` commits.

## 4. Trigger

Push (or merge a PR) to `main`. The full chain runs automatically:

```
push to main
  └─ CI  (lint · test · build · Docker push to GHCR)
       └─ E2E  (Cypress + Appium against SHA-tagged images)
            └─ Release  (api → common → web → mobile, sequential)
```

The Release workflow only starts if E2E concluded with `success`. A single failed E2E test blocks all releases for that commit.

## 5. What Each Release Produces

| Module | Tag | Artifacts |
|---|---|---|
| `api` | `api-v<version>` | GitHub Release · updated `build.gradle` · permanent GHCR image `ssv-api:<version>` |
| `common` | `common-v<version>` | GitHub Release · updated `package.json` |
| `web` | `web-v<version>` | GitHub Release · updated `package.json` · permanent GHCR image `ssv-web:<version>` |
| `mobile` | `mobile-v<version>` | GitHub Release · updated `package.json` · `app-debug.apk` release asset · permanent GHCR image `ssv-mobile:<version>` |

Version files and `CHANGELOG.md` are committed back to `main` with `[skip ci]` so the pipeline does not re-trigger.

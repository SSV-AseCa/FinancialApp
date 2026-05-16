---
type: architecture
created: 2026-05-16
---

# Monorepo Structure

All project modules live in a single repository. The structure separates concerns by deployment unit while keeping shared code in an explicit `packages/` layer.

## Directory Tree

```
/
├── api/                        # Backend REST API
├── packages/
│   └── ui-core/                # Shared UI components and logic (web + mobile)
├── apps/
│   ├── web/                    # Web frontend
│   │   └── cypress/            # Cypress E2E tests (co-located with web)
│   └── mobile/                 # Mobile application
│       └── appium/             # Appium E2E tests (co-located with mobile)
├── tests/
│   └── load/                   # Locust load and stress test scenarios
├── docker-compose.yml          # Full system definition (all services)
├── .github/
│   └── workflows/
│       └── ci.yml              # CI/CD pipeline
└── vault/                      # Project knowledge base
```

## Module Responsibilities

| Module | Owner | Responsibility |
|--------|-------|----------------|
| `api/` | API developers | REST API, domain logic, external integrations (EDGAR, Yahoo Finance), persistence |
| `packages/ui-core/` | Frontend Core | Shared UI components, form logic, auth state, API client |
| `apps/web/` | Web + Cypress | Web application consuming `ui-core`; Cypress tests in `cypress/` |
| `apps/mobile/` | Mobile + Appium | Mobile application consuming `ui-core`; Appium tests in `appium/` |
| `tests/load/` | Shared | Locust scenarios for load and stress testing against the deployed system |

## Dependency Graph

```
apps/web ──────┐
               ├──→ packages/ui-core ──→ api/
apps/mobile ───┘
```

- `ui-core` owns the API client — web and mobile never call the API directly
- Neither app knows about the other
- `api/` has no knowledge of any frontend module

## Key Config Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Defines all services: API, database, any background workers. Single `docker compose up` runs the full system |
| `.github/workflows/ci.yml` | Triggers on every push: build → unit tests → integration tests → E2E tests against the Docker environment |
| `api/Dockerfile` | API service image |
| `apps/web/Dockerfile` | Web application image |
| `apps/mobile/Dockerfile` | Mobile build image (if applicable) |

## Contracts

There are two dependency boundaries in this architecture, each requiring a contract agreed during cycle planning before development begins:

| Boundary | Contract | Agreed by |
|----------|----------|-----------|
| `api/` ↔ `ui-core` | Endpoint shape, request/response fields, error codes | API devs + Frontend Core |
| `ui-core` ↔ `apps/web` and `apps/mobile` | Component props, events, callbacks | Frontend Core + Web + Mobile |

Both contracts are agreed during cycle planning — not during development. Once they are in place, all five developers are unblocked simultaneously:

- **API dev** implements the endpoint against the agreed API contract
- **Core dev** implements the component against the agreed component interface
- **Web dev** builds the screen importing a local stub that matches the agreed component interface
- **Mobile dev** does the same with their own stub

```
Cycle planning
  ├── API contract agreed
  └── ui-core component interface agreed
           │
           ├── API dev: implements endpoint
           ├── Core dev: implements component
           ├── Web dev: builds screen against local component stub
           └── Mobile dev: builds screen against local component stub
                                    │
                              Integration (end of cycle)
                                    │
                    stubs → real ui-core → real API
                                    │
                            Cypress + Appium
```

At integration time, stubs are replaced with real `ui-core` imports. Screen code does not change because the interface was already respected throughout development.

## Notes

- Unit tests live inside their own module (`api/`, `packages/ui-core/`, `apps/web/`, `apps/mobile/`)
- Integration tests live inside `api/` since they exercise real external APIs (EDGAR, Yahoo Finance)
- Locust scenarios in `tests/load/` run against the full Docker-deployed system, not individual modules

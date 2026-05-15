---
type: constraints
created: 2026-05-15
---

# Delivery and Quality Constraints

Non-negotiable project requirements. Enforced through the [[Definition of Done]] and must all be in place before the final presentation.

## Deployment

- All services are defined in `docker-compose.yml` (API, database, background workers)
- Stateful services use persistent volumes
- The system runs end-to-end with a single `docker compose up`
- The environment is reproducible on any machine without host-specific configuration

## CI/CD Pipeline

- Pipeline configured on the chosen SaaS platform (GitHub Actions, GitLab CI, or Bitbucket Pipelines)
- Triggers on every push
- Executes: build → unit tests → integration tests
- Failures block merges
- E2E tests run against the Docker-deployed environment

## Code Quality

- No compiler warnings treated as errors left unresolved in merged code
- No commented-out code or debug artifacts in merged code
- Unit tests cover core domain logic (enforced per story via the DoD)
- Integration tests verify real external API behaviour — EDGAR and Yahoo Finance (enforced per story via the DoD)

## Load and Stress Testing

- Implemented with Locust
- At minimum two scenarios defined: normal load and peak/stress load
- Tests run against the Docker-deployed system
- Results documented and interpreted before final presentation

## Semantic Versioning

- Every merge to main produces a tagged release:
  - Patch (`x.y.Z`): bug fix, no interface change
  - Minor (`x.Y.0`): new investor-facing story delivered
  - Major (`X.0.0`): breaking change to a public interface

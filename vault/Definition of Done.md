---
type: definition-of-done
scope: global
created: 2026-05-14
---

# Definition of Done

This DoD applies to every story in this project. A story is considered done only when every applicable item below is satisfied. Items marked as conditional apply only when the story touches the relevant concern.

---

## Functional Correctness

- All acceptance criteria stated in the story are met without exception
- Edge cases and error paths described in the acceptance criteria are handled and tested
- No known defects remain open against the story

## Code Quality

- Code compiles and produces no warnings treated as errors
- No dead code, commented-out code, or debug artifacts are left in
- Public interfaces are intentional: nothing is exposed that is not needed by a caller

## Testing

- Unit tests cover the core domain logic introduced or modified by the story
- All existing tests continue to pass (no regressions)
- **(conditional — integration boundary)** If the story introduces or modifies a call to an external API (EDGAR, Yahoo Finance), at least one integration test exercises the real endpoint
- **(investor-facing stories)** The story's acceptance criteria explicitly include a Cypress test (web) and an Appium test (mobile); both must pass before the story is done
- Load and stress testing requirements are governed by [[Delivery and Quality Constraints]] and must be met before final delivery, not per story

## CI/CD

- The CI/CD pipeline passes on the branch before merging (build + all automated tests)
- No pipeline step is disabled or skipped to force a green build
- Full CI/CD and deployment requirements are specified in [[Delivery and Quality Constraints]]

## Deployability

- The system runs end-to-end via `docker compose up` with the story's changes included
- No manual steps are required beyond what is documented in the repository
- Full deployment requirements are specified in [[Delivery and Quality Constraints]]

## Versioning

- The change is reflected in the semantic version according to its nature:
  - Patch: bug fix with no interface change
  - Minor: new story delivering backward-compatible functionality
  - Major: breaking change to a public interface (requires explicit approval)

## Documentation

- **(conditional — new API endpoint)** The endpoint contract is documented (path, method, request/response shape, error codes)
- **(conditional — architectural decision)** Any non-obvious design choice is documented in the vault under `domain/` or `decisions/`

---
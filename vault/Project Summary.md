# Goal
- USA stock portfolio tracking web & Mobile app
- Integration of SEC's data, through the public API Edgar, and Yahoo finance's market prices
- API for the web and mobile app
# Requirements
## Functional
## Non-Functional
- The system must be **testable** through:
	- **Unit** tests
	- **Integration** tests (Including real external APIs)
	- **E2E** tests on real apps
- The system must **support performance validation** via:
	- **load** testing
	- **stress** testing
	- with defined **strategy** and **justification**
- The system must respect **external APIs limits**. specifically:
	- EDGAR rate limit (10 reqs/sec)
- The system must be **deployable** in a **reproductive environment** using **containerization**
- The system must support **automated** **build** and **test execution**
- The system **UI** (web/mobile) should be **minimal** enough to validate **core workflows only**
# Constraints
- Allowed programming languages:
	- C#, Java, Kotlin, Python, Ruby, Javascript, Typescript
- Required testing tools:
	- Unit: xUnit / Specs
	- E2E: Cypress / Appium
	- Stress: Locust
- The system must:
	- Use Docker Compose
	- Include persistent volumes
- The system must integrate with:
	- a SaaS platform (GitHub, GitLab, Bitbucket)
# Deliverable
- Report containing elected design and justification
- GitHub repository with the code
## Delivery Requirements
- Use **Semantic Versioning**
- Implement **CI/CD**
- Deliver via **Docker-based tooling**
- Perform a **final presentation**
# Dates
- **Start:** 23-04-2026
- **Presentation:** 18-06-2026
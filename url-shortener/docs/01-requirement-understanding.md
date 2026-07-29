# Requirement Understanding — URL Shortener Platform

## 1. Purpose

Build a production-grade URL Shortener platform and an accompanying Agentic Software Engineering Orchestrator. The platform must shorten valid long URLs, resolve short codes reliably, and expose operationally safe APIs suitable for production use.

The orchestrator must decompose work, enforce quality gates, preserve human approval points, and operate only within explicitly approved scope.

## 2. In-Scope Product Capabilities

The platform will provide the following capabilities:

- Create a shortened URL for a validated destination URL.
- Resolve a short code to its destination URL through an HTTP redirect.
- Support an optional caller-provided alias when it is available and valid.
- Support URL expiration and disablement so links can stop resolving safely.
- Record resolution activity asynchronously for analytics without delaying redirects.
- Provide authenticated management operations for links.
- Expose health, metrics, API documentation, structured logs, and audit-relevant events.
- Run locally and in CI using Docker, PostgreSQL, Redis, Kafka, Maven, and GitHub Actions.

## 3. Explicit Non-Functional Requirements

| Area | Requirement |
| --- | --- |
| Runtime | Java 21 and Spring Boot 3.x. |
| Data | PostgreSQL is the system of record; Redis is used only for cacheable read paths; Kafka carries asynchronous domain events. |
| API | REST API documented with OpenAPI/Swagger and versioned from its first release. |
| Quality | Unit tests use JUnit 5 and Mockito; integration tests use Testcontainers. |
| Operations | Micrometer and Prometheus-compatible metrics, health checks, structured logging, Docker support, and GitHub Actions CI. |
| Security | Validate and normalize URLs, prevent open-redirect abuse beyond explicitly stored destinations, protect management operations with authentication/authorization, and avoid logging sensitive values. |
| Reliability | A redirect must remain available when analytics processing is delayed or unavailable. Database state remains authoritative over cache state. |
| Maintainability | Clean Architecture boundaries, SOLID design, explicit domain language, and documentation maintained with the code. |

## 4. Domain Language

| Term | Meaning |
| --- | --- |
| Link | A managed mapping from one short code to one destination URL. |
| Short code | The generated or approved custom identifier used in a shortened URL. |
| Destination URL | The validated absolute URL to which a short code redirects. |
| Alias | A caller-selected short code that must meet policy and be unique. |
| Expired link | A link whose configured expiration instant has passed and must not redirect. |
| Disabled link | A link intentionally made unavailable and therefore not redirectable. |
| Resolution event | An asynchronous record emitted after a successful redirect decision. |
| Link owner | The authenticated principal allowed to manage a link. |

## 5. Primary User Journeys

1. A user submits a destination URL and receives a unique short URL.
2. A visitor opens the short URL and receives a redirect when the link is active and unexpired.
3. A link owner views, updates permitted metadata, disables, or expires their link.
4. An operations engineer observes health, latency, failures, cache behavior, and event-processing status.
5. The engineering orchestrator proposes bounded work, validates it against defined gates, and waits for human approval at required checkpoints.

## 6. Acceptance Criteria for the Finished Platform

- Creating a link with a valid URL returns a stable, unique short code and its complete short URL.
- Invalid, unsupported, malformed, unsafe, duplicate-alias, expired, or disabled link states return documented API outcomes.
- Resolving an active link redirects to exactly its stored destination URL.
- Redirect behavior does not depend on Kafka availability.
- Cache loss or staleness cannot permanently override the authoritative PostgreSQL state.
- Management operations enforce ownership and are auditable.
- API contracts, validation rules, errors, and security behavior appear in OpenAPI documentation.
- Automated unit and integration tests run in CI; PostgreSQL, Redis, and Kafka integration behavior is exercised with Testcontainers.
- Dockerized local execution and Prometheus scraping are documented and verifiable.
- The orchestrator records its planned action, scope, validation evidence, risks, and human approval decisions.

## 7. Controlled-Autonomy and Approval Model

The orchestrator may autonomously perform read-only analysis, create or modify code only inside an approved milestone, run validation, and prepare evidence. It must request human approval before:

- advancing to the next milestone;
- changing architecture or public API contracts after approval;
- applying database migrations in a shared or production environment;
- modifying security, retention, privacy, or operational policies;
- publishing releases, deploying, or changing external infrastructure.

Each milestone is intended to be a separately reviewable Git commit. This document is the Phase 1 checkpoint and does not authorize later-phase implementation.

## 8. Open Decisions to Resolve in Later Approved Phases

These decisions are intentionally deferred because they affect the BRD, functional specification, architecture, or security design:

- Tenant model and authentication provider.
- Public base URL and allowed redirect protocols/host policy.
- Short-code alphabet, length, collision strategy, and reserved-word policy.
- Link retention, analytics privacy policy, and event retention period.
- Target throughput, latency, availability objectives, and capacity assumptions.
- Whether custom domains, QR codes, bulk creation, and analytics dashboards are required.
- Deployment target and secrets-management solution.

## 9. Phase 1 Exit Criteria

Phase 1 is complete when the scope, constraints, terminology, acceptance criteria, controlled-autonomy boundaries, and deferred decisions above are accepted as the basis for the BRD.

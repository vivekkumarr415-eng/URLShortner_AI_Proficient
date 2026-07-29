# Business Requirements Document

## 1. Requirement Understanding

The product is a production-grade URL Shortener platform that converts validated destination URLs into compact, unique short URLs and redirects visitors safely and reliably. It will provide link lifecycle management, asynchronous resolution analytics, operational observability, and a controlled Agentic Software Engineering Orchestrator.

The platform is intended to be built with Java 21, Spring Boot 3, PostgreSQL, Redis, Kafka, Maven, Docker, GitHub Actions, Micrometer, Prometheus, OpenAPI, JUnit 5, Mockito, and Testcontainers. PostgreSQL will hold authoritative link data; Redis and Kafka will support performance and asynchronous processing without compromising redirect availability.

The orchestrator is a governed engineering capability, not an unbounded autonomous actor. It plans and validates work within approved scope, records evidence and risks, and pauses at human approval checkpoints.

## 2. Functional Requirements

| ID | Requirement |
| --- | --- |
| FR-01 | The system shall create a short URL for a valid destination URL. |
| FR-02 | The system shall generate a unique short code when an alias is not requested. |
| FR-03 | The system shall accept a requested custom alias only when it satisfies the configured policy and is available. |
| FR-04 | The system shall redirect a request for an active, unexpired short code to its stored destination URL. |
| FR-05 | The system shall reject creation requests containing malformed, unsupported, or unsafe destination URLs. |
| FR-06 | The system shall allow an authorized link owner to retrieve and manage links they own. |
| FR-07 | The system shall allow an authorized link owner to disable a link; disabled links shall not redirect. |
| FR-08 | The system shall support an optional expiration instant; an expired link shall not redirect. |
| FR-09 | The system shall publish a resolution event asynchronously after a successful redirect decision. |
| FR-10 | The system shall expose documented, versioned REST endpoints and documented error responses. |
| FR-11 | The system shall expose operational health and metrics endpoints. |
| FR-12 | The orchestrator shall record its task scope, planned actions, validation evidence, identified risks, and approval outcome for each engineering milestone. |
| FR-13 | The orchestrator shall require human approval before progressing to a new milestone or performing a restricted action. |

## 3. Non-Functional Requirements

| Area | Requirement |
| --- | --- |
| Technology | The backend shall use Java 21 and Spring Boot 3, with Maven as the build tool. |
| Data integrity | PostgreSQL shall be the system of record for link state. Cache or event-processing failure shall not change authoritative link state. |
| Performance | The redirect path shall be optimized for low latency and shall not synchronously depend on analytics event processing. Quantitative service objectives require product-owner approval. |
| Availability | A valid redirect shall remain possible when Kafka is unavailable; a Redis miss or cache eviction shall fall back to authoritative storage. |
| Security | Destination URLs shall be validated; management operations shall enforce authentication and authorization; sensitive values shall not be unnecessarily logged. |
| Observability | The service shall emit structured logs, health status, and Prometheus-compatible Micrometer metrics. |
| Testability | Unit tests shall use JUnit 5 and Mockito. Integration tests shall use Testcontainers for required infrastructure behavior. |
| API quality | REST contracts shall be versioned and documented in OpenAPI/Swagger. |
| Delivery | The application shall support containerized execution with Docker and automated CI with GitHub Actions. |
| Maintainability | The implementation shall follow SOLID, Clean Architecture, and domain-driven design where it provides a clear domain boundary. |

## 4. Stakeholders

| Stakeholder | Interest and Responsibility |
| --- | --- |
| Product Owner | Sets business priorities, target users, policy decisions, and accepts completed milestones. |
| End user / link owner | Creates and manages shortened links. |
| Link visitor | Uses short URLs and expects a safe, fast redirect. |
| Operations / SRE team | Operates, monitors, and responds to platform incidents. |
| Security and Compliance | Defines authentication, redirect safety, privacy, retention, and audit expectations. |
| Engineering team | Designs, implements, tests, documents, and maintains the platform. |
| QA team | Defines verification strategy and validates functional and non-functional outcomes. |
| Platform / DevOps team | Provides CI, container, deployment, secrets, monitoring, and infrastructure standards. |
| Engineering approver | Reviews orchestrator evidence and authorizes milestone progression. |

## 5. Assumptions

- The initial product serves HTTP API consumers and browser visitors.
- A link maps one short code to one immutable destination URL for its initial lifecycle; future edit semantics require explicit approval.
- Generated codes and aliases are globally unique within the initial public base URL.
- HTTPS is the only intended public redirect protocol; any exception requires security approval.
- PostgreSQL, Redis, and Kafka are available in development, test, and target deployment environments.
- Link management requires an authenticated identity, while resolving a public short URL does not.
- Resolution analytics will be handled asynchronously and are not required for the redirect response.
- Each project phase is one reviewable Git commit and requires explicit approval before the next phase starts.

## 6. Ambiguities

- The target user segments, commercial model, and expected traffic volume are unspecified.
- Authentication method, identity provider, roles, and tenant isolation are unspecified.
- The public base domain, custom-domain support, and redirect-code namespace are unspecified.
- Alias format, reserved words, short-code alphabet, length, and collision policy are unspecified.
- Link retention, deletion behavior, analytics fields, privacy classification, and event retention are unspecified.
- Required redirect status code and behavior for missing, disabled, and expired links are unspecified.
- Availability, latency, recovery, and scalability objectives are unspecified.
- Deployment environment, secrets-management system, and production topology are unspecified.

## 7. Risks

| Risk | Impact | Mitigation Direction |
| --- | --- | --- |
| Malicious destination URLs or open-redirect abuse | Security, reputation, and user harm | Strict URL validation, protocol policy, abuse controls, and security review. |
| Short-code collisions or alias takeover | Incorrect routing and loss of trust | Database-enforced uniqueness and an approved code-generation policy. |
| Cache inconsistency | Stale redirects or incorrect link state | Keep PostgreSQL authoritative and define cache invalidation and fallback behavior. |
| Kafka outage or consumer lag | Incomplete analytics | Decouple redirect success from event delivery and monitor event processing. |
| Analytics privacy exposure | Compliance and trust risk | Data minimization, retention policy, and security/compliance approval. |
| Unbounded growth | Cost and service degradation | Establish retention, capacity targets, and operational limits. |
| Undefined SLOs | Misaligned implementation and acceptance | Obtain measurable product and operations objectives before architecture approval. |
| Uncontrolled agent actions | Unsafe or unreviewed change | Enforce scope boundaries, approval gates, and auditable evidence. |

## 8. Questions for Product Owner

1. Who are the first-release users, and is the platform single-tenant or multi-tenant?
2. What authentication and authorization model is required for link management?
3. What public base URL will host short links, and are custom domains in scope?
4. Which redirect protocols and destination hosts are permitted?
5. Are custom aliases required in the first release? If yes, what syntax and reserved-word rules apply?
6. Should destination URLs be editable after creation, or should an edit create a new link?
7. What redirect response is required for active links, and what user experience is required for missing, expired, and disabled links?
8. Which analytics data is required, who can access it, and how long may it be retained?
9. What traffic, availability, latency, recovery, and data-retention objectives must the system meet?
10. Are QR codes, bulk operations, link tags, dashboards, rate limiting, or abuse reporting required for the first release?
11. What production environment, deployment process, and secrets-management standard must be used?
12. Which actions must always require human approval from the orchestrator?

## 9. Acceptance Criteria

- A valid destination URL can be shortened and returns a unique short URL.
- A valid, active, unexpired short URL redirects only to its stored destination URL.
- Invalid destination URLs and unavailable aliases are rejected with documented API errors.
- Disabled and expired links do not redirect and return documented outcomes.
- Only authorized owners can manage their links.
- Resolution-event processing does not delay or determine redirect success.
- Redis failure or eviction does not make PostgreSQL link data inaccessible to the redirect path.
- API behavior, validation rules, and errors are represented in OpenAPI documentation.
- Health, metrics, logs, and CI validation are available as defined by later approved engineering phases.
- The orchestrator creates an auditable record of scope, validation evidence, risk assessment, and approval before progressing.

## 10. Scope

### In Scope

- URL shortening and redirect resolution.
- Generated short codes and, subject to approved policy, custom aliases.
- Link ownership, expiration, disablement, and management APIs.
- Asynchronous resolution analytics events.
- PostgreSQL persistence, Redis caching, and Kafka messaging.
- REST API documentation, testing, monitoring, containerization, CI, and engineering documentation.
- A controlled, approval-gated engineering orchestrator process.

## 11. Out of Scope

- QR code generation.
- Custom branded domains.
- Bulk import or bulk link creation.
- End-user analytics dashboards and report exports.
- Paid plans, billing, quotas, and subscription management.
- Browser extensions, mobile applications, and user-facing web portals.
- Malware scanning, destination-content classification, and full anti-phishing services.
- Production deployment execution and external infrastructure provisioning until explicitly approved.

## 12. Success Metrics

| Metric | Initial Measurement |
| --- | --- |
| Redirect reliability | Percentage of valid active link resolutions that return the required redirect response. Target to be approved. |
| Redirect latency | Percentile latency for redirect responses. Target to be approved. |
| Link creation reliability | Percentage of valid creation requests completed successfully. Target to be approved. |
| Data correctness | Number of verified redirects that differ from the authoritative stored destination; target is zero. |
| Analytics delivery | Percentage of successful redirect events processed within the approved window. Target to be approved. |
| Security quality | Number of confirmed redirect-validation or authorization vulnerabilities; target is zero. |
| Delivery quality | CI pass rate and automated-test results for protected changes. Target to be approved. |
| Orchestration governance | Percentage of milestone transitions with recorded approval evidence; target is 100%. |

## 13. Engineering Goals

- Deliver a maintainable, testable Java service with clear domain and infrastructure boundaries.
- Make redirect correctness and availability the highest-priority technical behavior.
- Keep PostgreSQL authoritative and make cache and messaging integrations resilient to failure.
- Establish reproducible local, test, CI, and container workflows.
- Provide observable behavior with actionable logs, health checks, and metrics.
- Maintain secure defaults, explicit validation, least-privilege management access, and auditable changes.
- Operate the engineering orchestrator with bounded autonomy and mandatory approval gates.

## 14. Business Goals

- Provide a dependable URL-shortening capability for approved users and visitors.
- Improve sharing usability through compact, stable links.
- Preserve user trust by preventing unsafe routing and protecting link-management access.
- Enable informed product decisions through reliable resolution analytics.
- Establish a delivery process that is transparent, reviewable, and safe to extend.
- Create a foundation that can scale to future approved capabilities without compromising core redirect reliability.

---

## Phase 1 Approval Checkpoint

Approval of this BRD confirms the business baseline for the next phase. It does not approve functional-design, architecture, source-code, database, infrastructure, or deployment work.

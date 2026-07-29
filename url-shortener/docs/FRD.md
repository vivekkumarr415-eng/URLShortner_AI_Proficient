# Functional Requirements Document

## 1. Purpose

This document translates the approved business requirements into verifiable product behavior for the first release of the URL Shortener platform. It defines externally observable functionality and business rules; it does not prescribe REST resources, database schema, component structure, or implementation technology beyond the constraints already approved in the BRD.

## 2. Actors

| Actor | Description | Permitted Functional Interaction |
| --- | --- | --- |
| Anonymous visitor | A person or client following a public short URL. | Resolve an active public link. |
| Authenticated link owner | A verified principal who owns one or more links. | Create, view, and manage owned links. |
| Platform administrator | An authorized operational or support principal. | Access only the administrative functions explicitly approved in a later policy. |
| Operations engineer | A platform operator. | Observe service health and operational metrics. |
| Engineering approver | A human reviewer of orchestrator milestones. | Approve or reject progression to the next engineering milestone. |
| Engineering orchestrator | A governed automation actor. | Plan, validate, and record engineering work within approved boundaries. |

## 3. User Stories

| ID | User Story | Priority |
| --- | --- | --- |
| US-01 | As an authenticated link owner, I want to shorten a valid destination URL so that I can share a compact link. | Must |
| US-02 | As an authenticated link owner, I want to request a custom alias so that the link is memorable when the alias is available. | Must |
| US-03 | As an authenticated link owner, I want to set an expiration instant so that a link stops resolving after a defined time. | Must |
| US-04 | As a visitor, I want a short URL to redirect me quickly to the intended destination so that sharing is seamless. | Must |
| US-05 | As an authenticated link owner, I want to list and inspect my links so that I can manage them. | Must |
| US-06 | As an authenticated link owner, I want to disable one of my links so that it can no longer be used. | Must |
| US-07 | As an operations engineer, I want health status, metrics, and structured logs so that I can detect and diagnose failures. | Must |
| US-08 | As a product stakeholder, I want resolution events recorded asynchronously so that usage can be measured without slowing redirects. | Must |
| US-09 | As a security stakeholder, I want destination validation and protected management operations so that the service cannot be misused to route users unsafely or expose owner data. | Must |
| US-10 | As an engineering approver, I want each orchestrated milestone to include scope, validation evidence, risks, and an approval gate so that change is controlled. | Must |

## 4. Use Cases

### UC-01: Create Generated Short Link

**Primary actor:** Authenticated link owner  
**Preconditions:** The actor is authenticated and authorized to create links.  
**Trigger:** The actor submits a valid destination URL.

**Main flow:**

1. The system authenticates the actor.
2. The system validates the destination URL against the approved redirect policy.
3. The system generates and reserves a unique short code.
4. The system records the active link with its owner and optional expiration instant.
5. The system returns the created link summary and complete short URL.

**Alternate flows:**

- If validation fails, the system rejects the request without creating a link.
- If a generated code collides, the system retries allocation safely or returns a documented failure; it never creates an ambiguous mapping.
- If persistence cannot complete, the system does not return a successful creation outcome.

### UC-02: Create Custom-Alias Link

**Primary actor:** Authenticated link owner  
**Preconditions:** The actor is authenticated; the alias policy is configured.  
**Trigger:** The actor submits a destination URL and custom alias.

**Main flow:**

1. The system validates the destination URL.
2. The system validates the alias syntax and reserved-word policy.
3. The system verifies alias availability in the authoritative namespace.
4. The system creates the active link and associates it with the actor.
5. The system returns the created link summary.

**Alternate flows:**

- Invalid, reserved, or unavailable aliases are rejected without altering existing links.
- A race for the same alias is resolved by authoritative uniqueness; at most one request succeeds.

### UC-03: Resolve a Short Link

**Primary actor:** Anonymous visitor  
**Preconditions:** A short-code request reaches the platform.  
**Trigger:** The visitor opens a short URL.

**Main flow:**

1. The system locates the link state, using a cache only as a performance optimization.
2. The system verifies that the link is active and not expired.
3. The system responds with the approved redirect behavior to exactly the stored destination URL.
4. The system initiates asynchronous publication of a resolution event.

**Alternate flows:**

- For a missing, disabled, or expired code, the system returns the documented non-resolution outcome and does not reveal the destination URL.
- On a cache miss or cache failure, the system retrieves authoritative state.
- On analytics publication or processing failure, the redirect outcome remains unaffected and the failure is observable.

### UC-04: List, Retrieve, and Disable an Owned Link

**Primary actor:** Authenticated link owner  
**Preconditions:** The actor is authenticated.  
**Trigger:** The actor requests owned-link information or disablement.

**Main flow:**

1. The system authenticates the actor and verifies ownership.
2. For a list request, the system returns only the actor’s links using stable pagination.
3. For a retrieval request, the system returns the owned link’s allowed management details.
4. For disablement, the system records the disabled state authoritatively and ensures future resolutions are not eligible to redirect.
5. The system records the management action in the audit trail.

**Alternate flows:**

- The system denies attempts to access or change another owner’s link without disclosing protected details.
- Repeated disablement is handled as a documented idempotent or conflict outcome, to be finalized in API design.

### UC-05: Govern an Engineering Milestone

**Primary actor:** Engineering orchestrator  
**Supporting actor:** Engineering approver  
**Preconditions:** The milestone scope has been explicitly approved.  
**Trigger:** Work begins for a milestone.

**Main flow:**

1. The orchestrator records scope, intended actions, and known risks.
2. The orchestrator performs only in-scope analysis, changes, and validation.
3. The orchestrator records changed artifacts and validation evidence.
4. The orchestrator presents a milestone summary and proposed commit message.
5. The orchestrator stops and waits for explicit human approval before the next milestone.

**Alternate flow:** If scope expands or a restricted action is required, the orchestrator stops and requests human approval before proceeding.

## 5. Link Lifecycle

A link has one short code, one destination URL, one owner, and an optional expiration instant. A link is redirectable only while it is enabled and, where an expiration exists, before that instant.

| State | Redirectable | Entry Condition | Exit Condition |
| --- | --- | --- | --- |
| Active | Yes | Link is created enabled and has not expired. | Link is disabled or reaches its expiration instant. |
| Disabled | No | Owner disables an active link. | Re-enablement is not included in the initial release unless approved. |
| Expired | No | Current time reaches or exceeds the configured expiration instant. | None; the state is terminal for redirect purposes. |

## 6. Functional Requirements

### 6.1 Create Link

| ID | Requirement |
| --- | --- |
| FRD-CL-01 | An authenticated link owner shall be able to request creation of a link by providing a destination URL. |
| FRD-CL-02 | The system shall validate that the destination URL is absolute, syntactically valid, and permitted by the approved redirect policy. |
| FRD-CL-03 | The system shall reject a destination URL that is malformed, uses an unsupported protocol, contains invalid host information, or violates the approved redirect policy. |
| FRD-CL-04 | When no custom alias is supplied, the system shall allocate a unique generated short code. |
| FRD-CL-05 | When a custom alias is supplied, the system shall validate it against the approved alias policy and accept it only when it is available. |
| FRD-CL-06 | The system shall reject a requested alias that is invalid, reserved, or already assigned. |
| FRD-CL-07 | The system shall accept an optional future expiration instant. |
| FRD-CL-08 | The system shall reject an expiration instant that is not in the future at the time of creation. |
| FRD-CL-09 | On successful creation, the system shall persist the link as active and return its short code, complete short URL, destination URL, state, and expiration information. |
| FRD-CL-10 | The system shall associate the created link with the authenticated owner. |

### 6.2 Resolve Link

| ID | Requirement |
| --- | --- |
| FRD-RL-01 | An anonymous visitor shall be able to request resolution of a short code. |
| FRD-RL-02 | The system shall resolve an active, unexpired short code to exactly its stored destination URL. |
| FRD-RL-03 | The system shall respond to a successful resolution using the redirect behavior approved by the Product Owner. |
| FRD-RL-04 | The system shall not resolve a disabled or expired link. |
| FRD-RL-05 | The system shall not disclose the stored destination URL for a missing, disabled, or expired link. |
| FRD-RL-06 | The system shall return a documented outcome for a missing, disabled, or expired short code. |
| FRD-RL-07 | After a successful redirect decision, the system shall initiate asynchronous recording of one resolution event. |
| FRD-RL-08 | Failure or delay in asynchronous analytics processing shall not prevent a successful redirect. |

### 6.3 Retrieve and Manage Owned Links

| ID | Requirement |
| --- | --- |
| FRD-ML-01 | An authenticated link owner shall be able to retrieve an owned link by its identifier or short code through an approved management interface. |
| FRD-ML-02 | The system shall return the link’s short code, short URL, destination URL, state, creation information, and expiration information to its owner. |
| FRD-ML-03 | The system shall prevent a link owner from retrieving or changing a link owned by another principal. |
| FRD-ML-04 | An authenticated link owner shall be able to disable an active owned link. |
| FRD-ML-05 | Disabling a link shall make it non-redirectable immediately after the authoritative state change is completed. |
| FRD-ML-06 | Re-enabling a disabled link is out of scope for the initial release. |
| FRD-ML-07 | Changing a destination URL, alias, or ownership is out of scope for the initial release. |
| FRD-ML-08 | Permanent link deletion is out of scope for the initial release. |

### 6.4 Link Listing

| ID | Requirement |
| --- | --- |
| FRD-LL-01 | An authenticated link owner shall be able to list links they own. |
| FRD-LL-02 | The system shall not include links owned by other principals in an owner’s list. |
| FRD-LL-03 | The listing shall provide stable pagination behavior suitable for a growing collection. |
| FRD-LL-04 | The listing shall expose sufficient link summary data for an owner to identify status, destination, short URL, and expiration. |

### 6.5 Resolution Analytics Event

| ID | Requirement |
| --- | --- |
| FRD-AE-01 | The system shall create a resolution event only after an active link has been selected for a successful redirect. |
| FRD-AE-02 | A resolution event shall identify the resolved link and the time of the resolution decision. |
| FRD-AE-03 | The event payload shall include only analytics data approved by privacy and retention policy. |
| FRD-AE-04 | The event-processing path shall be observable for delivery failures and processing delay. |
| FRD-AE-05 | The initial release shall not expose an end-user analytics dashboard or reporting interface. |

### 6.6 Operational Interfaces

| ID | Requirement |
| --- | --- |
| FRD-OP-01 | Authorized operational tooling shall be able to determine whether the application is running and able to serve its required dependencies. |
| FRD-OP-02 | The system shall expose metrics for redirect requests, link-creation requests, errors, and integration health appropriate for platform monitoring. |
| FRD-OP-03 | The system shall produce structured logs sufficient to correlate failures without logging unnecessary sensitive data. |
| FRD-OP-04 | Public operational interfaces shall be protected according to the approved security policy. |

### 6.7 Engineering Orchestrator Governance

| ID | Requirement |
| --- | --- |
| FRD-OG-01 | For each milestone, the orchestrator shall record the requested scope, planned work, changed artifacts, validation performed, validation results, and material risks. |
| FRD-OG-02 | The orchestrator shall not begin a subsequent milestone without explicit human approval. |
| FRD-OG-03 | The orchestrator shall request human approval before architecture changes, public API changes after approval, shared or production database migration, security-policy changes, deployment, release publication, or external infrastructure changes. |
| FRD-OG-04 | The orchestrator may perform read-only analysis and in-scope implementation and validation only within the current approved milestone. |
| FRD-OG-05 | The orchestrator shall stop at the milestone boundary and provide a proposed commit message. |

## 7. Business Rules

| ID | Rule |
| --- | --- |
| BR-01 | A short code is globally unique within the initial public base URL namespace. |
| BR-02 | A generated short code and a custom alias share the same namespace. |
| BR-03 | A destination URL is stored only after passing validation. |
| BR-04 | A short code shall resolve only when its link is active and not expired. |
| BR-05 | The authoritative link state determines redirect eligibility. Cached data may improve performance but shall not override authoritative state. |
| BR-06 | Each created link has one owner and owner-restricted management access. |
| BR-07 | An expiration instant must be a future instant when a link is created. |
| BR-08 | A disabled link remains retained but cannot redirect. |
| BR-09 | Resolution analytics is best-effort relative to the redirect response; a processing failure must be observable. |
| BR-10 | No link-management operation may rely on client-provided ownership claims alone. |

## 8. API Expectations

The REST API contract will be defined in the approved API Design milestone. It shall meet these functional expectations:

| ID | Expectation |
| --- | --- |
| API-01 | Management operations shall use a versioned API namespace from the first release. |
| API-02 | The API shall use resource-oriented operations for link creation, retrieval, listing, and disablement. |
| API-03 | Redirect resolution shall use the public short-code path rather than a management API route. |
| API-04 | Create, retrieve, list, and disable operations shall return documented success and error outcomes. |
| API-05 | Validation failures shall identify the invalid field or rule without exposing internal implementation details. |
| API-06 | List operations shall support stable pagination and expose pagination metadata. |
| API-07 | API documentation shall be published in OpenAPI format and include security requirements, request validation, response schemas, and error schemas. |
| API-08 | Public redirect and management APIs shall define cache-control behavior appropriate to their distinct purposes. |
| API-09 | The exact endpoint paths, HTTP methods, status codes, media types, error envelope, and redirect status code are deferred to the API Design milestone. |

## 9. Validation and Error Behavior

The following outcomes must be documented in the later API contract. Exact HTTP status codes and error-envelope fields are intentionally deferred to the REST API Design phase.

| Condition | Required Outcome |
| --- | --- |
| Missing or invalid authentication for management action | Deny the action without exposing protected link data. |
| Attempt to manage another owner’s link | Deny the action without exposing protected link data. |
| Malformed or disallowed destination URL | Reject creation and identify the invalid request field. |
| Invalid, reserved, or unavailable alias | Reject creation and identify the alias issue. |
| Expiration instant not in the future | Reject creation and identify the expiration issue. |
| Missing short code | Return the documented non-resolution outcome without a destination URL. |
| Disabled or expired short code | Return the documented non-resolution outcome without a destination URL. |
| Duplicate generated code detected during creation | Retry generation or reject safely; never create ambiguous mappings. |

## 10. Analytics Requirements

| ID | Requirement |
| --- | --- |
| AN-01 | A successful redirect decision shall produce one logical resolution event associated with the resolved link. |
| AN-02 | The event shall include the link identifier and the timestamp of the redirect decision. |
| AN-03 | Event payload fields that could identify a visitor, device, or network must be approved by privacy policy before collection. |
| AN-04 | Event publication and consumption shall be observable through metrics and structured failure logs. |
| AN-05 | Event delivery shall be retried or otherwise handled according to an approved messaging reliability design; it shall not block redirect delivery. |
| AN-06 | Duplicate event handling must be safe for downstream analytics consumers. |
| AN-07 | Analytics retention, access rights, export, and deletion behavior require Product Owner and security approval before implementation. |

## 11. Security Requirements

| ID | Requirement |
| --- | --- |
| SEC-01 | Management operations shall require authentication using the identity mechanism approved by the Product Owner and security stakeholders. |
| SEC-02 | The system shall authorize link management by verified ownership and shall not trust client-supplied owner identifiers. |
| SEC-03 | Destination URLs shall be parsed and validated before persistence and before they become redirect targets. |
| SEC-04 | Only explicitly approved redirect protocols shall be accepted. The initial assumption is HTTPS-only. |
| SEC-05 | The system shall reject unsafe, malformed, or policy-disallowed destinations. Detailed host and network restrictions require security approval. |
| SEC-06 | Custom aliases shall be validated and protected against namespace collision, reserved-route collision, and unauthorized takeover. |
| SEC-07 | Error responses shall not reveal destination URLs, owner information, stack traces, credentials, or internal topology. |
| SEC-08 | Sensitive configuration and credentials shall be externally managed and excluded from source control and application logs. |
| SEC-09 | Operational endpoints shall have access controls appropriate to the deployment environment. |
| SEC-10 | Security-relevant management actions and authorization failures shall be auditable. |

## 12. Rate-Limiting Requirements

| ID | Requirement |
| --- | --- |
| RL-01 | The platform shall apply configurable rate limits to link-creation and management operations. |
| RL-02 | Rate limits shall be scoped to an authenticated principal and may additionally consider source identity according to approved security policy. |
| RL-03 | Public redirect traffic shall be protected from abuse using an approved edge or application-level strategy that preserves normal visitor access. |
| RL-04 | When a rate limit is exceeded, the system shall return a documented throttling outcome and must not perform the requested state-changing operation. |
| RL-05 | Rate-limit decisions, rejections, and configuration changes shall be observable. |
| RL-06 | Numeric rate limits, burst capacity, quotas, and bypass roles require Product Owner and operations approval before implementation. |

## 13. Performance and Capacity Targets

The initial BRD does not supply quantitative service-level objectives. The following are mandatory target definitions before production architecture approval:

| ID | Target to Approve |
| --- | --- |
| PERF-01 | Redirect latency at p50, p95, and p99 under expected and peak load. |
| PERF-02 | Link-creation latency at p50, p95, and p99 under expected and peak load. |
| PERF-03 | Sustained and peak redirect requests per second. |
| PERF-04 | Sustained and peak management requests per second. |
| PERF-05 | Maximum acceptable cache-miss and database-fallback behavior under load. |
| PERF-06 | Maximum acceptable resolution-event publication and processing delay. |
| PERF-07 | Availability objective, recovery-time objective, and recovery-point objective. |

Until targets are approved, engineering shall preserve the following qualitative requirements: redirect processing must not wait for analytics consumption; cache failure must fall back to authoritative data; and rate limiting must protect state-changing endpoints from avoidable load.

## 14. Failure-Handling Requirements

| Failure Condition | Required Behavior |
| --- | --- |
| PostgreSQL is unavailable for a required authoritative read or write | Do not return a false success, do not redirect using unverified stale data, return a documented temporary-failure outcome, and emit operational evidence. |
| Redis is unavailable or a cache entry is absent | Continue using authoritative storage when available; record cache health and failure evidence. |
| Kafka is unavailable during redirect | Preserve redirect behavior; make the analytics publication failure observable and handle it according to the approved reliability design. |
| Analytics consumer is delayed or unavailable | Preserve redirects, measure lag or failure, and process recoverable events according to the approved messaging design. |
| Generated-code collision occurs | Retry safely or return a documented creation failure; do not create duplicate mappings. |
| Alias collision occurs | Create no new conflicting link and return a documented availability outcome. |
| Authentication or authorization service is unavailable | Deny management operations safely unless an approved resilient-authentication strategy exists. |
| Invalid input is received | Reject it without partial state changes and provide the documented validation outcome. |
| Unexpected application failure occurs | Return a non-sensitive documented failure response, log correlation evidence, and expose error metrics. |

## 15. Monitoring Requirements

| ID | Requirement |
| --- | --- |
| MON-01 | The service shall expose liveness and readiness health information appropriate to orchestration and operations. |
| MON-02 | Metrics shall include redirect volume, redirect outcomes, redirect latency, link-creation volume, link-creation outcomes, validation failures, and authorization failures. |
| MON-03 | Metrics shall include cache hits, misses, failures, and fallback activity. |
| MON-04 | Metrics shall include analytics publication outcomes, consumer processing outcomes, and processing delay or lag where available. |
| MON-05 | Metrics shall include rate-limit decisions and rejections. |
| MON-06 | Metrics shall include database and messaging dependency health suitable for alerting. |
| MON-07 | Metric names, labels, cardinality limits, thresholds, dashboards, and alerts shall be designed in the Monitoring and Deployment phases. |

## 16. Logging Requirements

| ID | Requirement |
| --- | --- |
| LOG-01 | Application logs shall be structured and include timestamp, severity, service identity, environment, correlation identifier when available, and event category. |
| LOG-02 | Logs for management actions shall include the authenticated actor identifier in an approved pseudonymous or non-sensitive form. |
| LOG-03 | Logs for redirect outcomes shall include the short-code or link reference only when this does not violate approved privacy policy. |
| LOG-04 | Logs shall record validation failures, authorization denials, dependency failures, cache fallbacks, analytics failures, and unexpected errors. |
| LOG-05 | Logs shall not contain credentials, authorization tokens, raw secrets, unnecessary personal data, or full destination URLs unless explicitly approved. |
| LOG-06 | Log retention, access, masking, and export policies require operations and security approval. |

## 17. Audit Requirements

| ID | Requirement |
| --- | --- |
| AUD-01 | The system shall create an audit record for successful link creation. |
| AUD-02 | The system shall create an audit record for successful link disablement. |
| AUD-03 | Audit records shall include the action, time, actor, target link reference, and outcome. |
| AUD-04 | Authorization denials and security-relevant validation failures shall be auditable or otherwise retained in security logs according to approved policy. |
| AUD-05 | Audit data shall be protected from unauthorized modification and access. |
| AUD-06 | Audit retention, immutability, retrieval permissions, and external export requirements require compliance approval. |
| AUD-07 | Orchestrator audit records shall contain scope, artifact changes, validation evidence, material risks, approval decision, and milestone identity. |

## 18. Traceability to BRD

| BRD Requirement | Functional Detail |
| --- | --- |
| FR-01 to FR-03 | FRD-CL-01 to FRD-CL-10 |
| FR-04 | FRD-RL-01 to FRD-RL-08 |
| FR-05 | FRD-CL-02, FRD-CL-03, and validation behavior |
| FR-06 to FR-08 | FRD-ML-01 to FRD-ML-08 and FRD-LL-01 to FRD-LL-04 |
| FR-09 | FRD-AE-01 to FRD-AE-05 |
| FR-10 | API contract documentation requirement in later approved API-design milestone |
| FR-11 | FRD-OP-01 to FRD-OP-04 |
| FR-12 to FR-13 | FRD-OG-01 to FRD-OG-05 |

## 19. Deferred Decisions

The following items must be approved before their rules can be made final:

- Allowed destination protocols, host restrictions, and URL-normalization policy.
- Short-code format, alias format, reserved words, and collision handling policy.
- Redirect type and the outcomes for missing, disabled, and expired links.
- Authentication method, authorization roles, tenant model, and administrator capabilities.
- Public base URL and custom-domain policy.
- Analytics payload, privacy controls, retention period, and access policy.
- Quantitative latency, availability, recovery, and volume targets.

## 20. Functional Acceptance Criteria

- An authenticated owner can create a link with a valid destination URL and receive a unique short URL.
- The system rejects an invalid destination URL, invalid alias, unavailable alias, or non-future expiration with a documented validation outcome.
- An active, unexpired link resolves to precisely its stored destination.
- A missing, disabled, or expired link does not reveal its stored destination and returns its documented outcome.
- An owner can list, retrieve, and disable only their own links.
- A disabled link becomes non-redirectable after the authoritative state change.
- A successful redirect initiates one asynchronous resolution event without waiting for analytics processing.
- Management and redirect APIs expose documented security, validation, pagination, throttling, and error behavior.
- Operational health, metrics, structured logs, and audit records meet the expectations in this FRD.
- Dependency failures are handled without false successes and according to the required failure behavior.
- Each orchestrated milestone records scope, evidence, risks, and an explicit human approval checkpoint.

---

## Phase 2 Approval Checkpoint

Approval of this FRD authorizes the next milestone only. It does not authorize architecture, orchestration design, project bootstrap, source code, database schema, API implementation, deployment, or external infrastructure changes.

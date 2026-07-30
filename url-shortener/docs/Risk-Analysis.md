# Risk Analysis

## Risk Rating Methodology

Risks are rated by **Likelihood** (L) and **Impact** (I) on a scale of 1 (Low) to 3 (High). The **Risk Score** is `L × I`.

| Score | Severity | Action |
| ---: | --- | --- |
| 9 | Critical | Release blocker; must be resolved before production. |
| 6 | High | Must be mitigated before production; requires approval to defer. |
| 4 | Medium | Should be mitigated before production; track and monitor. |
| 1–3 | Low | Acceptable for baseline; address in future milestones. |

## Risk Register

### R1: Analytics Event Loss

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 3 (High) |
| Score | **6 — High** |
| Description | Analytics events may be lost when Analytics Service is unavailable and Feign retries are exhausted. The fallback logs the failure but does not persist the event. |
| Impact on System | Incomplete analytics reporting; click counts and aggregate reports will have gaps. |
| Current Mitigation | Async delivery with 3 retries, 100–500 ms backoff, circuit breaker, and fallback logging. |
| Required Mitigation | Implement a transactional outbox with Kafka publication for at-least-once delivery. Add delivery metrics and a replay policy. |

### R2: H2 Data Loss on Restart

| Attribute | Value |
| --- | --- |
| Likelihood | 3 (High) |
| Impact | 3 (High) |
| Score | **9 — Critical** |
| Description | All services use in-memory H2 databases. Data is destroyed when the service shuts down. |
| Impact on System | All short URLs, analytics events, and workflow records are lost. The platform cannot serve redirects after restart. |
| Current Mitigation | None; H2 is in-memory by design. |
| Required Mitigation | Migrate to managed PostgreSQL with Flyway/Liquibase migrations, automated backups, and restore testing. |

### R3: Unprotected Management APIs

| Attribute | Value |
| --- | --- |
| Likelihood | 3 (High) |
| Impact | 3 (High) |
| Score | **9 — Critical** |
| Description | No authentication or authorization is enforced. All management endpoints (create, update, delete, workflow operations) are open to anyone with network access. |
| Impact on System | Unauthorized users can create malicious short URLs, delete legitimate links, or manipulate workflows. |
| Current Mitigation | None. |
| Required Mitigation | Implement OIDC/JWT authentication, role-based access control, and API gateway enforcement. |

### R4: Redirect Abuse and Open-Redirect Risk

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 3 (High) |
| Score | **6 — High** |
| Description | While HTTPS-only validation is enforced, there is no URL blocklist, no rate limiting, and no abuse monitoring. Attackers could create short URLs pointing to phishing or malware sites. |
| Impact on System | Reputation damage, security harm to visitors, potential blocklisting of the platform's domain. |
| Current Mitigation | `@URL(protocol = "https")` validation on destination URLs. |
| Required Mitigation | Add URL safety policy (blocklist, domain reputation), rate limiting on creation, abuse monitoring, and redirect logging. |

### R5: No Load or Performance Evidence

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 2 (Medium) |
| Score | **4 — Medium** |
| Description | No load, soak, or failure-injection tests have been run. Performance characteristics under load are unknown. |
| Impact on System | Latency spikes, connection pool exhaustion, or availability degradation under production traffic. |
| Current Mitigation | None. |
| Required Mitigation | Conduct load tests (k6/Gatling), soak tests, and failure-injection tests. Define and validate SLOs. |

### R6: No Correlation IDs

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 2 (Medium) |
| Score | **4 — Medium** |
| Description | No correlation IDs are propagated across services or included in error responses. |
| Impact on System | Difficult to trace request flows and diagnose failures across services. |
| Current Mitigation | Structured logging via Logback. |
| Required Mitigation | Add a correlation ID filter, include IDs in logs and `ApiError` responses, and propagate via Feign headers. |

### R7: No Rate Limiting

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 2 (Medium) |
| Score | **4 — Medium** |
| Description | No rate limiting on any endpoint. The platform is vulnerable to abuse and resource exhaustion. |
| Impact on System | Denial of service, excessive short URL creation, analytics flooding. |
| Current Mitigation | None. |
| Required Mitigation | Add edge or application-level rate limits on creation, management, and redirect endpoints. |

### R8: IP and Referrer Privacy Exposure

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 3 (High) |
| Score | **6 — High** |
| Description | Analytics events store visitor IP addresses and referrer URLs without a privacy classification, retention policy, or access control. |
| Impact on System | Compliance violations (GDPR, CCPA), privacy harm to visitors, regulatory penalties. |
| Current Mitigation | IP address format validation; referrer is optional. |
| Required Mitigation | Data minimization (hash or truncate IPs), retention policy, PII classification, access controls, and privacy/compliance approval. |

### R9: No Secret Management

| Attribute | Value |
| --- | --- |
| Likelihood | 1 (Low) |
| Impact | 3 (High) |
| Score | **3 — Low** |
| Description | No secrets are currently used (H2 has no password, no API keys). However, production will require database credentials, identity provider secrets, and TLS keys. |
| Impact on System | If secrets are committed to source control or logged, they can be leaked. |
| Current Mitigation | No secrets in the codebase; service URLs are externalized. |
| Required Mitigation | Integrate with a secrets manager (Vault, AWS Secrets Manager, Kubernetes Secrets). Never commit secrets. |

### R10: No CI/CD Quality Gates

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 2 (Medium) |
| Score | **4 — Medium** |
| Description | No CI pipeline is configured. No automated formatting, static analysis, coverage threshold, or dependency scanning. |
| Impact on System | Code quality regressions, security vulnerabilities in dependencies, inconsistent formatting. |
| Current Mitigation | None. |
| Required Mitigation | Add GitHub Actions CI with compile, test, JaCoCo coverage gate, Spotless/Checkstyle, OWASP dependency-check, and container validation. |

### R11: No Database Migrations

| Attribute | Value |
| --- | --- |
| Likelihood | 2 (Medium) |
| Impact | 2 (Medium) |
| Score | **4 — Medium** |
| Description | Schema is managed by JPA annotations with `ddl-auto=none`. No migration tool is configured. |
| Impact on System | No controlled schema evolution; no rollback capability for schema changes. |
| Current Mitigation | JPA annotations define schema; H2 creates tables on startup. |
| Required Mitigation | Add Flyway or Liquibase with expand/contract migration strategy and versioned migration scripts. |

### R12: WorkflowEngine Complexity

| Attribute | Value |
| --- | --- |
| Likelihood | 1 (Low) |
| Impact | 2 (Medium) |
| Score | **2 — Low** |
| Description | `WorkflowEngine` is a large class with multiple responsibilities. |
| Impact on System | Maintenance difficulty, higher risk of bugs when modifying workflow logic. |
| Current Mitigation | Unit tests cover agent types and dependency graph. |
| Required Mitigation | Decompose into focused components as workflow behavior grows. |

## Risk Heat Map

```mermaid
quadrantChart
    title Risk Heat Map
    x-axis Low Likelihood --> High Likelihood
    y-axis Low Impact --> High Impact
    quadrant-1 Critical (Monitor closely)
    quadrant-2 High Impact / Low Likelihood
    quadrant-3 Low Priority
    quadrant-4 High Likelihood / Low Impact
    R2: [0.9, 0.9]
    R3: [0.9, 0.9]
    R1: [0.6, 0.8]
    R4: [0.6, 0.8]
    R8: [0.6, 0.8]
    R5: [0.6, 0.5]
    R6: [0.6, 0.5]
    R7: [0.6, 0.5]
    R10: [0.6, 0.5]
    R11: [0.6, 0.5]
    R9: [0.3, 0.8]
    R12: [0.3, 0.5]
```

## Release Gate Summary

| Gate | Status | Blocking Risks |
| --- | --- | --- |
| Durable storage | Not met | R2, R11 |
| Authentication/authorization | Not met | R3 |
| Security review | Not met | R4, R8, R9 |
| CI quality gates | Not met | R10 |
| Load/resilience evidence | Not met | R5 |
| Analytics reliability | Not met | R1 |
| Rate limiting | Not met | R7 |
| Observability (correlation) | Not met | R6 |

**Conclusion:** The platform is not production-ready. All critical and high risks must be mitigated before release.
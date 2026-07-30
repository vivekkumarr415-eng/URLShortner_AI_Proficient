# Limitations

## 1. Data Persistence

| Limitation | Impact | Workaround |
| --- | --- | --- |
| In-memory H2 database | All data is lost on service restart | Use for local development only. Production requires PostgreSQL. |
| No database migrations | No controlled schema evolution | JPA annotations define schema. Add Flyway or Liquibase for production. |
| No backup or restore | Data cannot be recovered after loss | Production requires automated backups and restore testing. |
| Per-service isolated databases | No cross-service data consistency | By design (microservice pattern). Use API contracts for cross-service communication. |

## 2. Security

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No authentication | All endpoints are open | Do not expose outside local development. Implement OIDC/JWT for production. |
| No authorization | No ownership checks on link management | Implement RBAC with ownership verification before production. |
| No TLS/mTLS | Traffic is unencrypted | Terminate TLS at edge. Configure mTLS for inter-service calls. |
| No rate limiting | Vulnerable to abuse and DoS | Add edge or application rate limits before production. |
| No secret management | No secrets are used currently | Integrate secrets manager before adding database credentials or API keys. |
| No URL blocklist | Open-redirect risk | Add URL safety policy with domain blocklist. |
| No reserved-word filtering | Aliases could collide with system paths | Add reserved-word list (e.g., `api`, `admin`, `r`, `analytics`). |

## 3. Analytics

| Limitation | Impact | Workaround |
| --- | --- | --- |
| Best-effort delivery only | Events may be lost on Analytics Service failure | Implement transactional outbox with Kafka for guaranteed delivery. |
| No event replay | Lost events cannot be recovered | Add durable event store with replay capability. |
| No delivery metrics | Cannot measure analytics delivery rate | Add Micrometer metrics for Feign success/failure/fallback. |
| In-memory aggregation | Top and daily analytics load all records into memory | Add database-level aggregation queries for production scale. |
| No retention policy | Analytics data grows indefinitely | Define and implement retention policy with privacy approval. |
| IP addresses stored in plaintext | PII exposure | Hash or truncate IPs. Implement data minimization. |

## 4. Performance and Scalability

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No caching layer | Every redirect reads from database | Add Redis cache with TTL-based invalidation. |
| No load testing | Performance under load is unknown | Conduct load, soak, and failure-injection tests. |
| No connection pooling tuning | Default HikariCP settings | Tune pool size based on load test results. |
| Single-instance per service | No horizontal scaling tested | Deploy multiple instances behind a load balancer. |
| No async processing framework | `@Async` uses default SimpleAsyncTaskExecutor | Configure a bounded `ThreadPoolTaskExecutor` for analytics publishing. |
| Synchronous redirect path | Database read on every redirect | Add Redis cache to reduce database load. |

## 5. Observability

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No correlation IDs | Cannot trace requests across services | Add correlation ID filter and propagate via Feign headers. |
| No metrics | No Prometheus/Micrometer metrics exposed | Add Micrometer registry and expose `/actuator/prometheus`. |
| No tracing | No distributed tracing | Add OpenTelemetry or Spring Cloud Sleuth. |
| No dashboards | No operational visibility | Configure Grafana dashboards for health, latency, and error rates. |
| No alerting | No automated incident detection | Configure Prometheus alerting rules. |
| Limited actuator exposure | Only `health` and `info` exposed | Add `metrics` and `prometheus` endpoints for production. |

## 6. Testing

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No coverage threshold | Coverage may regress | Add JaCoCo with minimum coverage gate. |
| No integration tests | H2 may not match PostgreSQL behavior | Add Testcontainers integration tests with PostgreSQL. |
| No contract tests | Feign contract may drift | Add Spring Cloud Contract tests. |
| No load tests | Performance unknown | Add k6 or Gatling load tests. |
| No end-to-end tests | Full flow not verified | Add end-to-end tests for create → redirect → analytics. |
| No security tests | Security posture unverified | Add OWASP ZAP scans and auth/authorization tests. |

## 7. CI/CD

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No CI pipeline | No automated quality gates | Add GitHub Actions with compile, test, coverage, and security scans. |
| No CD pipeline | No automated deployment | Add deployment pipeline with approval gates. |
| No container images | No Dockerfile or container build | Add Dockerfile per service and container build in CI. |
| No Docker Compose | No multi-service local orchestration | Add `compose.yaml` for local multi-service development. |
| No formatting enforcement | Code style may drift | Add Spotless or Checkstyle in CI. |

## 8. Orchestrator Service

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No durable work queues | Workflow state is in-memory | Use PostgreSQL with workflow state persistence. |
| No remote agent execution | Agents run in-process | Deploy agents as separate services if isolation is needed. |
| No concurrent workflow support | Single-threaded workflow execution | Add concurrent execution with locking if needed. |
| `WorkflowEngine` is a large class | Maintenance difficulty | Decompose into focused components. |
| No workflow history API | Cannot view past workflow states | Add workflow history endpoint with audit trail query. |
| No notification on approval gates | Approver must poll | Add webhook or event notification for approval requests. |

## 9. API Design

| Limitation | Impact | Workaround |
| --- | --- | --- |
| No pagination on list endpoints | Not applicable yet (no list endpoint implemented) | Add pagination when list endpoint is added. |
| No API versioning strategy | Version in path (`/api/v1/`) only | Define versioning policy for future contract changes. |
| No content negotiation | JSON only | Acceptable for current scope. Add content negotiation if needed. |
| No HATEOAS | No hypermedia links | Add HATEOAS links if REST maturity level 3 is required. |
| No request ID in responses | Cannot correlate client-side | Add request ID header and include in `ApiError`. |

## 10. Functional Gaps vs. FRD

| FRD Requirement | Status | Notes |
| --- | --- | --- |
| FRD-ML-03: Ownership-based access control | Not implemented | No authentication or ownership model. |
| FRD-LL-01 to LL-04: Link listing with pagination | Not implemented | No list endpoint exists. |
| FRD-AE-03: Privacy-approved analytics payload | Not implemented | IP and referrer stored without privacy classification. |
| FRD-AE-04: Observable event processing | Partially implemented | Fallback logging exists; no delivery metrics. |
| FRD-OP-02: Metrics for redirect/creation/errors | Not implemented | No Micrometer metrics exposed. |
| FRD-OP-03: Structured logs with correlation | Partially implemented | Structured logs exist; no correlation IDs. |
| SEC-01 to SEC-10: Security requirements | Not implemented | No auth, authorization, or audit trail. |
| RL-01 to RL-06: Rate limiting | Not implemented | No rate limiting on any endpoint. |
| MON-01 to MON-07: Monitoring requirements | Partially implemented | Health endpoints exist; no metrics or dashboards. |
| AUD-01 to AUD-07: Audit requirements | Not implemented | No audit trail for link management operations. |
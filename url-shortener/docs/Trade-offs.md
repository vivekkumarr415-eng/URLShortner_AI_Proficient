# Architectural and Engineering Trade-offs

## 1. Best-Effort Analytics vs. Guaranteed Delivery

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Analytics delivery | Best-effort via async OpenFeign with retry, circuit breaker, and fallback | Durable outbox with Kafka and at-least-once delivery | Redirect availability is the highest priority. Analytics must never block or fail a redirect. The current approach is simpler and sufficient for a baseline. |

**Trade-off:** Events may be lost after retries are exhausted and the fallback is invoked. The redirect succeeds, but analytics reporting will have gaps.

**Future path:** Implement a transactional outbox pattern with Kafka publication for guaranteed delivery, as described in the [Architecture](Architecture.md) document.

## 2. In-Memory H2 vs. PostgreSQL

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Database | H2 in-memory per service | Managed PostgreSQL with migrations | H2 enables zero-infrastructure local development and testing. No external database is needed to run or test the platform. |

**Trade-off:** Data is lost on restart. No durability, backup, or multi-instance guarantees. H2 does not support the full PostgreSQL feature set (e.g., JSONB, advanced indexing).

**Future path:** Migrate to PostgreSQL with Flyway or Liquibase migrations, connection pooling, and backup/restore procedures.

## 3. Synchronous JPA vs. Caching Layer

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Read path | Direct JPA read on every redirect | Redis cache with PostgreSQL fallback | Simplicity and correctness. No cache invalidation complexity. Every redirect reads authoritative state. |

**Trade-off:** Higher latency under load without a cache. Database is on the critical path for every redirect.

**Future path:** Add Redis as a performance layer with TTL-based invalidation, as described in the Architecture document. Cache misses fall back to PostgreSQL.

## 4. Per-Service Database vs. Shared Database

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Database topology | Independent H2 per service | Shared PostgreSQL with schema-per-service | Service independence. Each service owns its data. No shared schema coupling. |

**Trade-off:** No cross-service joins. Analytics cannot directly query URL Service data. Data consistency across services is eventual.

**Future path:** Maintain per-service databases in production (PostgreSQL per service) with well-defined API contracts for cross-service communication.

## 5. Feign Client vs. Message Broker

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Inter-service communication | Spring Cloud OpenFeign (HTTP) | Kafka event-driven | Direct HTTP call is simpler for a baseline. Feign provides declarative client, timeout, retry, and circuit breaker. |

**Trade-off:** Tighter coupling between URL Service and Analytics Service. URL Service must know Analytics Service's URL. No event replay or buffering.

**Future path:** Introduce Kafka for analytics event publication with a transactional outbox, decoupling the services and enabling replay.

## 6. Concrete Service Classes vs. Interface Abstractions

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Service design | Concrete `@Service` classes | Interface + implementation pairs | Simplicity. Spring manages singletons. Less boilerplate. |

**Trade-off:** Controllers depend on concrete classes, not interfaces. This limits mockability and violates full dependency inversion.

**Future path:** Extract service interfaces (`ShortUrlService` → `ShortUrlService` interface + `ShortUrlServiceImpl`) as the codebase grows and multiple implementations are needed.

## 7. WorkflowEngine as a Single Class vs. Decomposed Components

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Orchestrator design | Single `WorkflowEngine` class handling state, dispatch, audit, and response mapping | Separate state manager, agent dispatcher, audit recorder, and response assembler | Rapid implementation. All workflow logic in one transactional boundary. |

**Trade-off:** The class is large and has multiple responsibilities. It will become harder to maintain as workflow behavior grows.

**Future path:** Decompose into `WorkflowStateService`, `AgentDispatcher`, `AuditRecorder`, and `WorkflowResponseAssembler` while maintaining transactional integrity.

## 8. OpenAPI/Swagger vs. Contract-First Design

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| API design | Code-first with Springdoc OpenAPI annotations | Contract-first with OpenAPI YAML and code generation | Faster implementation. Annotations are co-located with code. Swagger UI is auto-generated. |

**Trade-off:** API contract is derived from code, not defined independently. Contract changes require code changes. No formal contract review or versioning.

**Future path:** Consider extracting OpenAPI specs to YAML files and using code generation for controllers and DTOs, enabling contract-first design and independent contract review.

## 9. No Authentication vs. OIDC/JWT

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Security | No authentication/authorization | OIDC with JWT tokens and RBAC | Focus on core functionality first. Authentication requires identity provider selection and policy approval. |

**Trade-off:** All management endpoints are open. Anyone with network access can create, modify, or delete short URLs and workflows.

**Future path:** Implement OIDC authentication with JWT tokens, role-based access control, and an API gateway enforcement layer. This is a critical release blocker.

## 10. Java 17 vs. Java 21

| Dimension | Choice | Alternative | Rationale |
| --- | --- | --- | --- |
| Java version | Java 17 (LTS) | Java 21 (LTS) | Java 17 is widely supported and stable. The BRD mentions Java 21, but the implementation uses 17 for broader compatibility. |

**Trade-off:** Missing Java 21 features (pattern matching for switch, record patterns, virtual threads). Virtual threads could improve redirect throughput.

**Future path:** Upgrade to Java 21 to leverage virtual threads for high-concurrency redirect handling and pattern matching for cleaner workflow state transitions.
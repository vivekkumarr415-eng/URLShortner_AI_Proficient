# Quality and Operations Baseline

## Coverage and verification

The Maven reactor publishes a JaCoCo HTML/XML report for every service during `mvn verify`. Reports are written to each module's `target/site/jacoco/` directory and are intended to make coverage changes visible in CI and code review. The suite covers service rules, repository queries, redirect behavior, analytics fallback, workflow transitions, API-key protection, and rate-limit rejection.

The current quality target is **70% line coverage for changed business logic**. The target is deliberately applied as a CI policy rather than an unverified local claim: once the baseline report has been accepted, the pipeline should enforce it with JaCoCo's `check` goal and prevent regression. Integration, contract, load, and security suites remain separate release gates because unit coverage alone cannot demonstrate production behavior.

## Security controls now present

| Control | Scope | Behaviour |
| --- | --- | --- |
| API-key authentication | URL management API (`/api/v1/urls/**`) | Requests must send `Authorization: ApiKey <URL_SERVICE_API_KEY>`; redirects remain public. A missing key fails closed with 401. |
| Rate limiting | URL creation/management and redirects | Per-instance, per-client fixed window; limit is configurable with `URL_SERVICE_RATE_LIMIT` and excess requests receive 429 with `Retry-After`. |
| Input safety | URL and analytics request DTOs | HTTPS destinations, bounded fields, and URL-safe aliases are validated before persistence. |
| Payload exposure | Error handling | Domain handlers return safe envelopes without stack traces or destination URLs. |
| Operational endpoints | Actuator | Only `health` and `info` are exposed. |

The in-process limiter is appropriate for local and single-instance deployments. A production multi-instance deployment must replace or supplement it with a shared gateway/Redis limiter so limits are global.

## Secrets and environments

No secret is stored in the repository. Deployments inject secrets through the environment or a secrets manager:

| Variable | Purpose |
| --- | --- |
| `URL_SERVICE_API_KEY` | Required credential for link-management endpoints. |
| `URL_SERVICE_DB_URL` | Production PostgreSQL JDBC URL. |
| `URL_SERVICE_DB_USERNAME` / `URL_SERVICE_DB_PASSWORD` | PostgreSQL credentials. |
| `URL_SERVICE_RATE_LIMIT` | Per-client requests allowed per minute (default: 120). |

Activate the production database profile with `SPRING_PROFILES_ACTIVE=prod`. It uses PostgreSQL and `ddl-auto=validate`, so schema changes must be introduced through reviewed migrations rather than automatic DDL. H2 remains the local-development and test database only.

## Release gates

Before public production exposure, add OIDC/JWT with user ownership checks, mTLS/TLS at the edge and between services, PostgreSQL migrations/backups, a shared rate limit, a managed secrets provider, dependency scanning/SBOM, contract tests for the analytics client, Testcontainers PostgreSQL tests, and load/failure-injection evidence. These are intentionally tracked as release gates rather than represented as already complete.

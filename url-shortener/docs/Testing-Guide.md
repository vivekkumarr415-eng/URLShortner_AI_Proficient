# Testing Guide

Run the complete reactor suite from `url-shortener/`:

```bash
mvn clean verify
```

Run one module with `mvn -pl url-service test`. Unit and JPA tests cover short URL lifecycle behavior, redirect delegation, analytics aggregation and persistence, OpenFeign publisher fallback behavior, and orchestrator agents/dependency graph.

Before release, add and enforce integration tests against the production database, contract tests for the URL-to-Analytics API, authenticated API tests, resilience tests for timeout/retry/fallback, and load tests for redirect latency. Publish coverage with a configured coverage tool; no coverage threshold is currently enforced.

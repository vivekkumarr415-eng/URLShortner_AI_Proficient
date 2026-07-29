# URL Shortener Agentic Engineering System

This repository is a Maven multi-module bootstrap for a URL Shortener platform. Each module is an independently runnable Spring Boot 3 application using Java 17. This commit establishes only the service foundations; it intentionally contains no URL-shortening, analytics, orchestration, or gateway business logic.

## Services

| Service | Default port | Responsibility |
| --- | ---: | --- |
| `api-gateway` | 8080 | Edge-facing application boundary for future request routing, cross-cutting API policy, and gateway observability. |
| `url-service` | 8081 | Authoritative future home for short-link creation, resolution, lifecycle, and H2-backed link data. |
| `analytics-service` | 8082 | Future home for asynchronous resolution-analytics ingestion and query capabilities. |
| `orchestrator-service` | 8083 | Future home for governed Agentic SDLC orchestration, approval state, and audit records. |

## Prerequisites

- Java 17
- Maven 3.9 or later

## Build and Test

Run the reactor build from this directory:

```bash
mvn clean verify
```

Run an individual service, for example:

```bash
mvn -pl url-service spring-boot:run
```

Each service exposes Spring Boot Actuator health at `/actuator/health` and Swagger UI at `/swagger-ui/index.html` when running. H2 consoles are deliberately disabled; database access will be defined in the data-design and implementation milestones.

## Documentation

Completed requirements, architecture, and orchestration documentation is in [`docs/`](docs/).

## Repository Layout

```text
url-shortener/
├── api-gateway/
├── url-service/
├── analytics-service/
├── orchestrator-service/
├── docs/
├── pom.xml
└── README.md
```

## Current Scope

The service modules provide dependency management, application entry points, configuration, OpenAPI metadata, validation infrastructure, consistent error handling, structured logging, and health monitoring. Implementing business endpoints and persistence behavior requires a later approved milestone.

## Persistence Model

This project currently uses independent in-memory H2 databases for the services that own data. The database model is intentionally limited to JPA entities, constraints, indexes, repository contracts, validated DTOs, and repository tests; no controllers or service-layer business behavior are included.

| Service | Model | Persistence Responsibility |
| --- | --- | --- |
| `url-service` | `ShortUrl` | Stores original URL, generated short code, optional custom alias, creation and expiration times, active status, and click count. Unique constraints protect short codes and aliases. |
| `analytics-service` | `ClickAnalytics` | Stores a click event with short code, click time, IP address, browser, device, operating system, and referrer. Query indexes support short-code and time-window lookups. |
| `orchestrator-service` | `WorkflowExecution`, `ApprovalHistory`, `WorkflowState` | Stores workflow state and append-only approval decisions associated with an execution. `WorkflowState` is a persisted enum; approval records are indexed by workflow and decision time. |

Repository tests use Spring Data JPA's H2 test support to verify persistence mappings and derived repository queries.

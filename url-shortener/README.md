# URL Shortener Platform

A Java 17 / Spring Boot 3.3 microservice platform for short-link management, redirect analytics, and approval-governed engineering workflows.

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| API Gateway | 8080 | Edge-service foundation. |
| URL Service | 8081 | Creates, manages, and resolves short URLs. |
| Analytics Service | 8082 | Stores redirect events and exposes aggregate reporting. |
| Orchestrator Service | 8083 | Runs auditable, approval-gated SDLC workflows. |

URL Service publishes a best-effort analytics event after a successful redirect through Spring Cloud OpenFeign. The client uses a 500 ms connect timeout, 1 s read timeout, bounded retry, circuit breaker, and fallback; a delivery failure never changes the redirect response.

## Quick Start

Prerequisites: Java 17 and Maven 3.9+.

```bash
cd url-shortener
mvn clean verify
mvn -pl url-service spring-boot:run
```

Run additional services in separate terminals by replacing `url-service` with `analytics-service` or `orchestrator-service`. Health is available at `/actuator/health`; Swagger UI is available at `/swagger-ui.html`.

## Documentation

| Document | Description |
| --- | --- |
| [API documentation](docs/API-Documentation.md) | REST endpoints, schemas, and error contracts. |
| [Architecture](docs/Architecture.md) | System context, component design, and data models. |
| [Setup guide](docs/Setup-Guide.md) | Local development and configuration. |
| [Testing guide](docs/Testing-Guide.md) | Test strategy and execution. |
| [Production-readiness review](docs/Production-Readiness-Review.md) | Engineering review and release gates. |
| [Trade-offs](docs/Trade-offs.md) | Architectural trade-off analysis. |
| [Risk analysis](docs/Risk-Analysis.md) | Risk register with mitigations. |
| [Security review](docs/Security-Review.md) | Security controls and gaps. |
| [Limitations](docs/Limitations.md) | Known limitations. |
| [Engineering summary](docs/Engineering-Summary.md) | Final engineering summary. |
| [Agent orchestration design](docs/Agent-Orchestration.md) | Agentic SDLC orchestrator. |
| [BRD](docs/BRD.md) | Business Requirements Document. |
| [FRD](docs/FRD.md) | Functional Requirements Document. |

## Repository Structure

```text
url-shortener/
├── api-gateway/             # Gateway foundation
├── url-service/             # Short URL management and redirect service
├── analytics-service/       # Event ingestion and reporting
├── orchestrator-service/    # Agentic SDLC workflow engine
├── docs/                    # Product, architecture, API, and readiness docs
└── pom.xml                  # Maven reactor and dependency management
```

## Production Status

This repository is a production-oriented engineering baseline, not a production deployment. It uses independent in-memory H2 stores, has no authentication/authorization enforcement, and requires external database, secret-management, observability, security, and load-validation work before release. See the [production-readiness review](docs/Production-Readiness-Review.md) for the explicit risk register and release gates.
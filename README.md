# PSTProject — URL Shortener Agentic Engineering System

A production-oriented **Java 17 / Spring Boot 3.3** microservice platform for short-link management, redirect analytics, and approval-governed engineering workflows.

## Quick Start

```bash
cd url-shortener
mvn clean verify
mvn -pl url-service spring-boot:run
```

Prerequisites: **Java 17** and **Maven 3.9+**.

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| API Gateway | 8080 | Edge-service foundation. |
| URL Service | 8081 | Creates, manages, and resolves short URLs. |
| Analytics Service | 8082 | Stores redirect events and exposes aggregate reporting. |
| Orchestrator Service | 8083 | Runs auditable, approval-gated SDLC workflows. |

## Documentation Index

| Document | Description |
| --- | --- |
| [README](url-shortener/README.md) | Project overview and quick start. |
| [API Documentation](url-shortener/docs/API-Documentation.md) | REST endpoints, request/response schemas, and error contracts. |
| [Architecture](url-shortener/docs/Architecture.md) | System context, component design, sequences, and data models. |
| [Setup Guide](url-shortener/docs/Setup-Guide.md) | Local development and configuration instructions. |
| [Testing Guide](url-shortener/docs/Testing-Guide.md) | Test strategy, execution, and coverage expectations. |
| [Production Readiness Review](url-shortener/docs/Production-Readiness-Review.md) | Engineering review, release gates, and readiness assessment. |
| [Trade-offs](url-shortener/docs/Trade-offs.md) | Architectural and engineering trade-off analysis. |
| [Risk Analysis](url-shortener/docs/Risk-Analysis.md) | Risk register with impact and mitigation. |
| [Security Review](url-shortener/docs/Security-Review.md) | Security controls, gaps, and release blockers. |
| [Limitations](url-shortener/docs/Limitations.md) | Known limitations and constraints. |
| [Engineering Summary](url-shortener/docs/Engineering-Summary.md) | Final engineering summary and deliverables. |
| [BRD](url-shortener/docs/BRD.md) | Business Requirements Document. |
| [FRD](url-shortener/docs/FRD.md) | Functional Requirements Document. |
| [Agent Orchestration](url-shortener/docs/Agent-Orchestration.md) | Agentic SDLC orchestrator design. |

## Repository Structure

```text
PSTProject/
├── README.md                         # This file
└── url-shortener/                    # Maven reactor root
    ├── pom.xml                       # Parent POM and dependency management
    ├── README.md                     # Module README
    ├── api-gateway/                  # Gateway foundation (port 8080)
    ├── url-service/                  # Short URL management and redirect (port 8081)
    ├── analytics-service/            # Event ingestion and reporting (port 8082)
    ├── orchestrator-service/         # Agentic SDLC workflow engine (port 8083)
    └── docs/                         # All engineering documentation
```

## Production Status

This repository is a **production-oriented engineering baseline**, not a production deployment. It uses in-memory H2 stores, has no authentication/authorization enforcement, and requires external database, secret-management, observability, security, and load-validation work before release. See the [Production Readiness Review](url-shortener/docs/Production-Readiness-Review.md) for the explicit risk register and release gates.
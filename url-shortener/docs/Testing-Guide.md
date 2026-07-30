# Testing Guide

## Test Strategy

The platform uses a layered testing approach:

| Layer | Scope | Tools | Status |
| --- | --- | --- | --- |
| Unit tests | Service logic, domain rules, utility classes | JUnit 5, Mockito, AssertJ | Implemented |
| Repository tests | JPA queries and entity mapping | Spring Boot Test, H2 | Implemented |
| Context tests | Spring application context loads | `@SpringBootTest` | Implemented |
| Integration tests | Production database, external services | Testcontainers | Not yet implemented |
| Contract tests | URL-to-Analytics Feign contract | Spring Cloud Contract | Not yet implemented |
| Load tests | Redirect latency and throughput | k6 / Gatling | Not yet implemented |
| Security tests | Auth, authorization, input abuse | OWASP tools, custom tests | Not yet implemented |

## Running Tests

### Full reactor suite

```bash
cd url-shortener
mvn clean verify
```

### Single module

```bash
mvn -pl url-service test
mvn -pl analytics-service test
mvn -pl orchestrator-service test
mvn -pl api-gateway test
```

### Single test class

```bash
mvn -pl url-service test -Dtest=ShortUrlServiceTest
```

### Skip tests (for quick compilation)

```bash
mvn clean compile -DskipTests
```

## Current Test Inventory

### URL Service

| Test Class | Type | What It Verifies |
| --- | --- | --- |
| `ShortUrlServiceTest` | Unit (Mockito) | Creates short URL with generated code; rejects duplicate alias; rejects inactive/expired links during redirect; updates active state and destination. |
| `RedirectControllerTest` | Unit (MockMvc) | Redirect delegation to service; analytics event publication after redirect; 302 response with Location header. |
| `ShortUrlRepositoryTest` | Repository (H2) | JPA queries: `findByShortCodeOrCustomAlias`, `existsByShortCodeOrCustomAlias`. |
| `AnalyticsEventPublisherTest` | Unit (Mockito) | Async event delivery through Feign client. |
| `AnalyticsServiceClientFallbackFactoryTest` | Unit | Fallback logs event and does not rethrow on analytics failure. |
| `UrlServiceApplicationTests` | Context | Spring application context loads successfully. |

### Analytics Service

| Test Class | Type | What It Verifies |
| --- | --- | --- |
| `AnalyticsServiceTest` | Unit (Mockito) | Records redirect events; aggregates top and daily click counts correctly. |
| `ClickAnalyticsRepositoryTest` | Repository (H2) | JPA queries for click event persistence and retrieval. |
| `AnalyticsServiceApplicationTests` | Context | Spring application context loads successfully. |

### Orchestrator Service

| Test Class | Type | What It Verifies |
| --- | --- | --- |
| `WorkflowAgentsTest` | Unit | Agent types are correctly exposed (`REQUIREMENT`, `REVIEW`, `APPROVAL`). |
| `WorkflowDependencyGraphTest` | Unit | Stage dependencies and next-stage transitions are correct. |
| `WorkflowExecutionRepositoryTest` | Repository (H2) | Workflow execution entity persistence. |
| `ApprovalHistoryRepositoryTest` | Repository (H2) | Approval history entity persistence. |
| `OrchestratorServiceApplicationTests` | Context | Spring application context loads successfully. |

### API Gateway

| Test Class | Type | What It Verifies |
| --- | --- | --- |
| `ApiGatewayApplicationTests` | Context | Spring application context loads successfully. |

## Test Conventions

- **Naming:** Test classes end with `Test` (e.g., `ShortUrlServiceTest`). Test methods use descriptive names (e.g., `rejectsDuplicateCustomAlias`).
- **Assertions:** AssertJ fluent assertions (`assertThat(...)`) are used throughout.
- **Mocking:** Mockito with `@ExtendWith(MockitoExtension.class)` and `@Mock` annotations.
- **Time:** `Clock` is injected and fixed in tests using `Clock.fixed(...)` for deterministic expiry checks.
- **Repositories:** `@DataJpaTest` with H2 for JPA layer tests.
- **Context:** `@SpringBootTest` for application context load verification.

## Coverage

No coverage threshold is currently enforced. To add coverage reporting:

1. Add JaCoCo plugin to the parent POM:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

2. Run `mvn clean verify` to generate reports in `target/site/jacoco/`.

## Required Tests Before Release

| Test Type | Description | Priority |
| --- | --- | --- |
| Integration tests | Test against PostgreSQL via Testcontainers instead of H2. | Critical |
| Contract tests | Verify URL-to-Analytics Feign contract with Spring Cloud Contract. | Critical |
| Authenticated API tests | Test all management endpoints with authentication and authorization. | Critical |
| Resilience tests | Verify timeout, retry, circuit breaker, and fallback under failure injection. | High |
| Load tests | Measure redirect p50/p95/p99 latency and throughput under load. | High |
| End-to-end tests | Full redirect flow: create → redirect → analytics event → analytics report. | High |
| Security tests | Input validation, URL policy, rate limiting, abuse detection. | High |
| Soak tests | Sustained load over hours to detect memory leaks and degradation. | Medium |
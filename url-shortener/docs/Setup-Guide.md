# Setup Guide

## Prerequisites

| Requirement | Version | Verification |
| --- | --- | --- |
| Java JDK | 17 or newer | `java -version` |
| Maven | 3.9 or newer | `mvn -version` |
| Git | Any recent version | `git --version` |

## 1. Clone and Build

```bash
git clone https://github.com/vivekkumarr415-eng/PSTProject.git
cd PSTProject/url-shortener
mvn clean verify
```

The `mvn clean verify` command compiles all four modules, runs unit and JPA tests, and packages each service. All tests use in-memory H2 and require no external infrastructure.

## 2. Start Services

Each service runs independently on its own port. Open a separate terminal for each.

### URL Service (port 8081)

```bash
mvn -pl url-service spring-boot:run
```

### Analytics Service (port 8082)

```bash
mvn -pl analytics-service spring-boot:run
```

### Orchestrator Service (port 8083)

```bash
mvn -pl orchestrator-service spring-boot:run
```

### API Gateway (port 8080)

```bash
mvn -pl api-gateway spring-boot:run
```

## 3. Verify Services

| Check | URL |
| --- | --- |
| URL Service health | `http://localhost:8081/actuator/health` |
| Analytics Service health | `http://localhost:8082/actuator/health` |
| Orchestrator Service health | `http://localhost:8083/actuator/health` |
| API Gateway health | `http://localhost:8080/actuator/health` |
| URL Service Swagger UI | `http://localhost:8081/swagger-ui.html` |
| Analytics Service Swagger UI | `http://localhost:8082/swagger-ui.html` |
| Orchestrator Service Swagger UI | `http://localhost:8083/swagger-ui.html` |

Each health endpoint should return `{"status":"UP"}`.

## 4. Quick API Walkthrough

### Create a short URL

```bash
curl -X POST http://localhost:8081/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com","customAlias":"demo"}'
```

### Follow the redirect

```bash
curl -v http://localhost:8081/r/demo
```

Expected: `302 Found` with `Location: https://example.com`.

### Check analytics

```bash
curl http://localhost:8082/analytics/demo
```

### Start a workflow

```bash
curl -X POST http://localhost:8083/workflow/start \
  -H "Content-Type: application/json" \
  -d '{"workflowName":"feature-x","context":{"scope":"demo"}}'
```

## 5. Configuration

Configuration is in each module's `src/main/resources/application.yml`.

### Key Configuration Properties

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | Service-specific | HTTP port (8080–8083). |
| `server.shutdown` | `graceful` | Enables graceful shutdown. |
| `spring.datasource.url` | `jdbc:h2:mem:...` | In-memory H2 database URL. |
| `spring.jpa.hibernate.ddl-auto` | `none` | No auto-DDL; schema is managed by JPA annotations. |
| `spring.jpa.open-in-view` | `false` | Disables OSIV for predictable transaction boundaries. |
| `analytics-service.base-url` | `http://localhost:8082` | Analytics Service URL for Feign client. |
| `spring.cloud.openfeign.circuitbreaker.enabled` | `true` | Enables Resilience4j circuit breaker. |
| `spring.cloud.openfeign.client.config.analytics-service.connectTimeout` | `500` | Feign connect timeout in ms. |
| `spring.cloud.openfeign.client.config.analytics-service.readTimeout` | `1000` | Feign read timeout in ms. |
| `management.endpoints.web.exposure.include` | `health,info` | Actuator endpoint exposure. |
| `management.endpoint.health.probes.enabled` | `true` | Kubernetes liveness/readiness probes. |
| `springdoc.api-docs.path` | `/v3/api-docs` | OpenAPI JSON path. |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI path. |
| `logging.level.com.example.urlshortener` | `INFO` | Application log level. |

### Feign Retry Configuration

The `AnalyticsFeignConfiguration` class configures:

| Parameter | Value | Description |
| --- | --- | --- |
| Retry initial interval | 100 ms | Initial backoff. |
| Retry max interval | 500 ms | Maximum backoff. |
| Retry max attempts | 3 | Maximum retry attempts. |

### External Configuration for Deployed Environments

For deployed environments, provide an environment-specific Analytics Service URL, database credentials, and secrets through external configuration (environment variables, Spring Cloud Config, or Kubernetes ConfigMaps/Secrets). **Never commit credentials.**

```bash
# Example environment variables
export ANALYTICS_SERVICE_BASE_URL=https://analytics.internal.example.com
export SPRING_DATASOURCE_URL=jdbc:postgresql://db.internal:5432/urlservice
export SPRING_DATASOURCE_USERNAME=urlservice_app
export SPRING_DATASOURCE_PASSWORD=$SECRET_DB_PASSWORD
```

## 6. Database

H2 is appropriate only for local development. Each service uses an independent in-memory H2 database that is destroyed on shutdown.

Production requires managed PostgreSQL or another approved durable database, schema migrations (Flyway or Liquibase), backups, encryption, and retention controls. See the [Architecture](Architecture.md) document for the target database design.

## 7. Logging

Each service uses Logback with a `logback-spring.xml` configuration. Logs are written to the console at `INFO` level for application packages. Adjust log levels via configuration:

```yaml
logging:
  level:
    com.example.urlshortener: DEBUG
    org.springframework.web: DEBUG
```

## 8. Troubleshooting

| Problem | Solution |
| --- | --- |
| Port already in use | Check for running processes on ports 8080–8083 and stop them. |
| Analytics events not delivered | Ensure Analytics Service is running on port 8082. Check logs for fallback warnings. |
| Build fails | Ensure Java 17 and Maven 3.9+ are installed. Run `mvn clean verify -X` for debug output. |
| H2 database is empty on restart | Expected behavior; H2 is in-memory. Data is lost on shutdown. |
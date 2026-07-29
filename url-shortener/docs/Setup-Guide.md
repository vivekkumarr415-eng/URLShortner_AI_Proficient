# Setup Guide

1. Install Java 17 and Maven 3.9 or newer.
2. From `url-shortener/`, run `mvn clean verify`.
3. Start URL, Analytics, and Orchestrator services independently with `mvn -pl <service> spring-boot:run`.
4. Verify each service with `GET /actuator/health`.

Configuration is in each module's `src/main/resources/application.yml`. `analytics-service.base-url` defaults to `http://localhost:8082`. For deployed environments, provide an environment-specific Analytics Service URL, database credentials, and secrets through external configuration; never commit credentials.

H2 is appropriate only for local development. Production requires managed PostgreSQL or another approved durable database, migrations, backups, encryption, and retention controls.

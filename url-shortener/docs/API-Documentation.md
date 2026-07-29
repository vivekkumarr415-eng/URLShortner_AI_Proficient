# API Documentation

All services publish OpenAPI JSON at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

## URL Service (`:8081`)

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/urls` | Create a destination URL with an optional alias and expiration. |
| GET | `/api/v1/urls/{shortCode}` | Retrieve metadata for a short code or alias. |
| PUT | `/api/v1/urls/{id}` | Update URL metadata and activation state. |
| DELETE | `/api/v1/urls/{id}` | Delete a short URL. |
| GET | `/r/{shortCode}` | Return `302 Found` for an active, unexpired link. |

Successful redirects trigger `POST /analytics/events` asynchronously. Validation errors use the shared `ApiError` envelope; missing links return 404 and inactive/expired links return 410.

## Analytics Service (`:8082`)

| Method | Path | Description |
| --- | --- | --- |
| POST | `/analytics/events` | Internal ingestion endpoint for redirect data. |
| GET | `/analytics/{shortCode}` | Total clicks and browser/device/OS/referrer breakdowns. |
| GET | `/analytics/top?limit=10` | Most-clicked short codes, up to 100. |
| GET | `/analytics/daily?from=YYYY-MM-DD&to=YYYY-MM-DD` | UTC daily totals; defaults to the prior 30 days. |

## Orchestrator Service (`:8083`)

| Method | Path | Description |
| --- | --- | --- |
| POST | `/workflow/start` | Start a workflow and record initial context. |
| GET | `/workflow/{id}` | Retrieve state, stage, plan revision, retry count, and context. |
| POST | `/workflow/{id}/approve` | Record a human approval, rework request, rejection, or deferral. |
| POST | `/workflow/{id}/retry` | Retry a failed or safely stopped workflow. |
| POST | `/workflow/{id}/rollback` | Record rollback and transition to safe stop. |

All management and analytics endpoints must be protected by an approved authentication and authorization layer before external exposure.

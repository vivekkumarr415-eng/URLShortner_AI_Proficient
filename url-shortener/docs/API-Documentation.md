# API Documentation

All services publish OpenAPI JSON at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

## Shared Error Envelope

All services use a shared `ApiError` record for error responses:

```json
{
  "timestamp": "2026-07-30T04:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "originalUrl: originalUrl must be a valid HTTPS URL",
  "path": "/api/v1/urls"
}
```

| Field | Type | Description |
| --- | --- | --- |
| `timestamp` | `Instant` (ISO-8601) | Time the error was generated. |
| `status` | `int` | HTTP status code. |
| `error` | `String` | HTTP reason phrase. |
| `message` | `String` | Human-readable error detail; never includes stack traces, credentials, or destination URLs. |
| `path` | `String` | Request path that triggered the error. |

### Error Status Codes

| Status | Condition |
| ---: | --- |
| 400 | Validation failure (malformed URL, invalid alias, non-future expiry). |
| 404 | Short URL or workflow not found. |
| 409 | Duplicate custom alias or database constraint violation. |
| 410 | Short URL is inactive or expired (redirect path only). |
| 500 | Unexpected internal failure. |

---

## URL Service (`:8081`)

### 1. Create Short URL

`POST /api/v1/urls`

Creates a short URL with an optional custom alias and expiration date.

**Request Body — `ShortUrlCreateRequest`**

| Field | Type | Required | Validation | Description |
| --- | --- | --- | --- | --- |
| `originalUrl` | `String` | Yes | `@NotBlank`, `@Size(max=2048)`, `@URL(protocol=https)` | Destination URL; must be a valid HTTPS URL. |
| `customAlias` | `String` | No | `@Pattern(^[A-Za-z0-9_-]{3,32}$)` | Custom alias; 3–32 URL-safe characters. |
| `expiryDate` | `Instant` | No | `@Future` | Optional expiration timestamp; must be in the future. |

**Example Request:**

```json
{
  "originalUrl": "https://example.com/products",
  "customAlias": "products",
  "expiryDate": "2026-12-31T23:59:59Z"
}
```

**Response — `201 Created`**

Headers: `Location: /api/v1/urls/{publicCode}`

**Response Body — `ShortUrlResponse`**

| Field | Type | Description |
| --- | --- | --- |
| `id` | `Long` | Internal database identifier. |
| `originalUrl` | `String` | Stored destination URL. |
| `shortCode` | `String` | System-generated short code (6–32 chars). |
| `customAlias` | `String` | Custom alias if provided, otherwise `null`. |
| `publicCode` | `String` | The alias if provided, otherwise the generated short code. |
| `createdAt` | `Instant` | Creation timestamp. |
| `expiryDate` | `Instant` | Expiration timestamp or `null`. |
| `active` | `boolean` | Whether the link is active. |
| `clickCount` | `long` | Number of redirect clicks recorded. |

**Example Response:**

```json
{
  "id": 1,
  "originalUrl": "https://example.com/products",
  "shortCode": "a1B2c3D4",
  "customAlias": "products",
  "publicCode": "products",
  "createdAt": "2026-07-30T04:00:00Z",
  "expiryDate": "2026-12-31T23:59:59Z",
  "active": true,
  "clickCount": 0
}
```

**Errors:** `400` (invalid URL/alias/expiry), `409` (duplicate alias).

---

### 2. Get Short URL Metadata

`GET /api/v1/urls/{shortCode}`

Retrieves metadata for a short code or custom alias.

**Path Parameter:**

| Parameter | Validation | Description |
| --- | --- | --- |
| `shortCode` | `@Pattern(^[A-Za-z0-9_-]{3,32}$)` | Short code or custom alias. |

**Response — `200 OK`**: `ShortUrlResponse` (see above).

**Errors:** `400` (invalid format), `404` (not found).

---

### 3. Update Short URL

`PUT /api/v1/urls/{id}`

Updates URL metadata and activation state.

**Path Parameter:** `id` (`Long`) — internal database identifier.

**Request Body — `ShortUrlUpdateRequest`**

| Field | Type | Required | Validation | Description |
| --- | --- | --- | --- | --- |
| `originalUrl` | `String` | Yes | `@NotBlank`, `@Size(max=2048)`, `@URL(protocol=https)` | Updated destination URL. |
| `customAlias` | `String` | No | `@Pattern(^[A-Za-z0-9_-]{3,32}$)` | Updated custom alias. |
| `expiryDate` | `Instant` | No | `@Future` | Updated expiration timestamp. |
| `active` | `Boolean` | Yes | `@NotNull` | Whether the link is active. |

**Response — `200 OK`**: `ShortUrlResponse`.

**Errors:** `400` (validation), `404` (not found), `409` (duplicate alias).

---

### 4. Delete Short URL

`DELETE /api/v1/urls/{id}`

Permanently deletes a short URL.

**Path Parameter:** `id` (`Long`).

**Response — `204 No Content`**: Empty body.

**Errors:** `404` (not found).

---

### 5. Redirect

`GET /r/{shortCode}`

Resolves a short code or custom alias and returns a `302 Found` redirect to the original URL.

**Path Parameter:**

| Parameter | Validation | Description |
| --- | --- | --- |
| `shortCode` | `@Pattern(^[A-Za-z0-9_-]{3,32}$)` | Short code or custom alias. |

**Response — `302 Found`**

Headers: `Location: <original URL>`

**Errors:** `404` (not found), `410` (inactive or expired).

After a successful redirect, URL Service asynchronously publishes a `POST /analytics/events` request to Analytics Service via Spring Cloud OpenFeign. Analytics delivery is best-effort and never affects the redirect response.

---

## Analytics Service (`:8082`)

### 1. Record Redirect Event

`POST /analytics/events`

Internal ingestion endpoint for redirect event data. Called asynchronously by URL Service after a successful redirect.

**Request Body — `ClickAnalyticsCreateRequest`**

| Field | Type | Required | Validation | Description |
| --- | --- | --- | --- | --- |
| `shortCode` | `String` | Yes | `@NotBlank`, `@Pattern(^[A-Za-z0-9_-]{3,32}$)` | Resolved short code. |
| `clickedAt` | `Instant` | Yes | `@NotNull` | Timestamp of the redirect decision. |
| `ipAddress` | `String` | Yes | `@NotBlank`, `@Pattern(^[0-9a-fA-F:.]+$)`, `@Size(max=45)` | Visitor IP address (IPv4 or IPv6). |
| `browser` | `String` | Yes | `@NotBlank`, `@Size(max=255)` | Browser name. |
| `device` | `String` | Yes | `@NotBlank`, `@Size(max=255)` | Device type. |
| `operatingSystem` | `String` | Yes | `@NotBlank`, `@Size(max=255)` | Operating system. |
| `referrer` | `String` | No | `@Size(max=2048)` | Referrer URL or `null`. |

**Response — `201 Created`**: `ClickAnalyticsResponse`.

**Errors:** `400` (validation failure).

---

### 2. Get Analytics Summary

`GET /analytics/{shortCode}`

Returns total clicks and breakdowns by browser, device, OS, and referrer.

**Path Parameter:**

| Parameter | Validation | Description |
| --- | --- | --- |
| `shortCode` | `@Pattern(^[A-Za-z0-9_-]{3,32}$)` | Short code to query. |

**Response — `200 OK` — `AnalyticsSummaryResponse`**

| Field | Type | Description |
| --- | --- | --- |
| `shortCode` | `String` | Queried short code. |
| `totalClicks` | `long` | Total click count. |
| `browsers` | `Map<String,Long>` | Click counts by browser. |
| `devices` | `Map<String,Long>` | Click counts by device type. |
| `operatingSystems` | `Map<String,Long>` | Click counts by OS. |
| `referrers` | `Map<String,Long>` | Click counts by referrer (blank = "Direct"). |

---

### 3. Get Top Short Codes

`GET /analytics/top?limit={limit}`

Returns the most-clicked short codes, sorted by click count descending.

**Query Parameter:**

| Parameter | Default | Validation | Description |
| --- | --- | --- | --- |
| `limit` | `10` | `@Min(1)`, `@Max(100)` | Maximum number of results. |

**Response — `200 OK`**: `List<TopAnalyticsResponse>`

| Field | Type | Description |
| --- | --- | --- |
| `shortCode` | `String` | Short code. |
| `clicks` | `long` | Total click count. |

---

### 4. Get Daily Click Totals

`GET /analytics/daily?from={from}&to={to}`

Returns UTC daily click totals for a date range.

**Query Parameters:**

| Parameter | Default | Description |
| --- | --- | --- |
| `from` | `to - 29 days` | Start date (inclusive, `YYYY-MM-DD`). |
| `to` | Today (UTC) | End date (inclusive, `YYYY-MM-DD`). |

**Response — `200 OK`**: `List<DailyAnalyticsResponse>`

| Field | Type | Description |
| --- | --- | --- |
| `date` | `LocalDate` | UTC date. |
| `clicks` | `long` | Click count for that day. |

**Errors:** `400` (if `from` is after `to`).

---

## Orchestrator Service (`:8083`)

### 1. Start Workflow

`POST /workflow/start`

Starts a new engineering workflow and records initial context.

**Request Body — `WorkflowStartRequest`**

| Field | Type | Required | Validation | Description |
| --- | --- | --- | --- | --- |
| `workflowName` | `String` | Yes | `@NotBlank`, `@Size(max=120)` | Human-readable workflow name. |
| `context` | `Map<String,String>` | No | — | Initial key-value context entries. |

**Response — `200 OK` — `WorkflowDetailsResponse`**

| Field | Type | Description |
| --- | --- | --- |
| `id` | `UUID` | Workflow identifier. |
| `workflowName` | `String` | Workflow name. |
| `workflowState` | `WorkflowState` | Current state (see below). |
| `currentStage` | `WorkflowStage` | Current stage (see below). |
| `planRevision` | `int` | Plan revision count (incremented on replan). |
| `retryCount` | `int` | Number of retries. |
| `approvalRound` | `int` | Current approval round. |
| `createdAt` | `Instant` | Creation timestamp. |
| `updatedAt` | `Instant` | Last update timestamp. |
| `context` | `Map<String,String>` | Stored context entries. |

**`WorkflowState` Enum:** `PENDING`, `RUNNING`, `AWAITING_APPROVAL`, `APPROVED`, `REJECTED`, `COMPLETED`, `FAILED`, `CANCELLED`, `ROLLING_BACK`, `SAFE_STOPPED`.

**`WorkflowStage` Enum:** `REQUIREMENTS`, `PLANNING`, `ARCHITECTURE`, `IMPLEMENTATION`, `TESTING`, `DOCUMENTATION`, `REVIEW`, `APPROVAL`.

---

### 2. Get Workflow Details

`GET /workflow/{id}`

Retrieves the current state, stage, plan revision, retry count, and context of a workflow.

**Path Parameter:** `id` (`UUID`).

**Response — `200 OK`**: `WorkflowDetailsResponse`.

**Errors:** `404` (workflow not found).

---

### 3. Submit Approval

`POST /workflow/{id}/approve`

Records a human approval, rework request, rejection, or deferral.

**Path Parameter:** `id` (`UUID`).

**Request Body — `WorkflowApprovalRequest`**

| Field | Type | Required | Validation | Description |
| --- | --- | --- | --- | --- |
| `decision` | `ApprovalDecision` | Yes | `@NotNull` | `APPROVED`, `REJECTED`, `REWORK_REQUESTED`, or `DEFERRED`. |
| `approver` | `String` | Yes | `@NotBlank`, `@Size(max=120)` | Approver identity. |
| `comments` | `String` | No | `@Size(max=2000)` | Optional comments. |

**Response — `200 OK`**: `WorkflowDetailsResponse`.

**Errors:** `404` (not found), `409` (workflow not in `AWAITING_APPROVAL` state).

---

### 4. Retry Workflow

`POST /workflow/{id}/retry`

Retries a failed or safely stopped workflow.

**Path Parameter:** `id` (`UUID`).

**Response — `200 OK`**: `WorkflowDetailsResponse`.

**Errors:** `404` (not found), `409` (workflow is not `FAILED` or `SAFE_STOPPED`).

---

### 5. Rollback Workflow

`POST /workflow/{id}/rollback`

Records a rollback and transitions the workflow to `SAFE_STOPPED`.

**Path Parameter:** `id` (`UUID`).

**Response — `200 OK`**: `WorkflowDetailsResponse`.

**Errors:** `404` (not found), `409` (workflow is `COMPLETED`).

---

## Security Note

All management and analytics endpoints must be protected by an approved authentication and authorization layer before external exposure. The public redirect endpoint (`GET /r/{shortCode}`) is the only endpoint intended for unauthenticated access.
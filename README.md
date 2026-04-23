# Smart Campus Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey)** and an embedded **Grizzly HTTP server** for managing university rooms and IoT sensors across a smart campus.

---

## Table of Contents
1. [API Overview](#api-overview)
2. [Project Structure](#project-structure)
3. [How to Build & Run](#how-to-build--run)
4. [Sample curl Commands](#sample-curl-commands)
5. [Report: Question Answers](#report-question-answers)

---

## API Overview

| Resource       | Base Path              | Description                        |
|----------------|------------------------|------------------------------------|
| Discovery      | `GET /api/v1`          | API metadata and resource links    |
| Rooms          | `/api/v1/rooms`        | Create, list, fetch, delete rooms  |
| Sensors        | `/api/v1/sensors`      | Register, list, filter sensors     |
| Readings       | `/api/v1/sensors/{id}/readings` | Historical reading log per sensor |

**Base URL:** `http://localhost:8080/api/v1`

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                            ← Starts embedded Grizzly server
    ├── AppConfig.java                       ← @ApplicationPath("/api/v1")
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── storage/
    │   └── DataStore.java                   ← Thread-safe in-memory ConcurrentHashMap store
    ├── resource/
    │   ├── DiscoveryResource.java           ← GET /api/v1
    │   ├── RoomResource.java                ← /api/v1/rooms
    │   ├── SensorResource.java              ← /api/v1/sensors
    │   └── SensorReadingResource.java       ← sub-resource /sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── LinkedResourceNotFoundException.java
    │   ├── SensorUnavailableException.java
    │   └── mappers/
    │       └── ExceptionMappers.java        ← 409, 422, 403, 500 mappers
    └── filter/
        └── LoggingFilter.java               ← Logs all requests and responses
```

---

## How to Build & Run

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/ShadowReaper404/SmartCampusAPI.git
cd SmartCampusAPI
```

**2. Build the project (creates a single runnable JAR)**
```bash
mvn package
```

**3. Start the server**
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

**4. Confirm it's running**
```
===========================================
 Smart Campus API is running!
 Base URL : http://localhost:8080/
 Discovery: http://localhost:8080/api/v1
 Press ENTER to stop the server.
===========================================
```

The API is now accessible at `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Discovery — get API metadata and resource links
```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```
**Expected response:**
```json
{
  "api": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "contact": "admin@smartcampus.ac.uk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a new Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Lab","capacity":40}'
```
**Expected response (201 Created):**
```json
{
  "id": "ENG-201",
  "name": "Engineering Lab",
  "capacity": 40,
  "sensorIds": []
}
```

---

### 3. Get all Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms \
  -H "Accept: application/json"
```

---

### 4. Register a new Sensor (linked to an existing room)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-002","type":"CO2","status":"ACTIVE","currentValue":400.0,"roomId":"ENG-201"}'
```
**Expected response (201 Created):**
```json
{
  "id": "CO2-002",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 400.0,
  "roomId": "ENG-201"
}
```

---

### 5. Filter Sensors by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

---

### 6. Post a Sensor Reading (updates currentValue automatically)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 520.5}'
```
**Expected response (201 Created):**
```json
{
  "id": "a1b2c3d4-...",
  "timestamp": 1713340800000,
  "value": 520.5
}
```

---

### 7. Get all Readings for a Sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/CO2-002/readings \
  -H "Accept: application/json"
```

---

### 8. Delete a Room that still has Sensors (expect 409 error)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```
**Expected response (409 Conflict):**
```json
{
  "error": "ROOM_NOT_EMPTY",
  "message": "Room LIB-301 still has 1 sensor(s) assigned.",
  "roomId": "LIB-301",
  "sensors": 1
}
```

---

### 9. Register Sensor with invalid roomId (expect 422 error)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'
```
**Expected response (422 Unprocessable Entity):**
```json
{
  "error": "LINKED_RESOURCE_NOT_FOUND",
  "message": "Referenced roomId 'FAKE-999' does not exist.",
  "missingRoomId": "FAKE-999"
}
```

---

### 10. Post reading to a MAINTENANCE sensor (expect 403 error)

First set a sensor to MAINTENANCE (or use seed data), then:
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 25.0}'
```
*(Only triggers 403 if the sensor status is "MAINTENANCE")*

---

## Report: Question Answers

---

### Part 1 — Q1: JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a **new instance of a resource class for every incoming HTTP request**. This is called **per-request scope**. Each request gets its own fresh object, which avoids issues with shared mutable state between requests.

However, this has a critical implication for in-memory data storage. If each resource instance held its own `HashMap`, data written in one request would be invisible to all future requests — each request would start with a blank map.

To solve this, this project uses a **Singleton DataStore** (`DataStore.getInstance()`). All resource instances share the exact same `DataStore` object, so data persists across requests. Furthermore, because multiple requests can arrive simultaneously (concurrent threads), the DataStore uses `ConcurrentHashMap` instead of a plain `HashMap`. `ConcurrentHashMap` is thread-safe — it uses internal locking to prevent two threads from corrupting the same map entry at the same time. A plain `HashMap` under concurrent access can cause data loss, infinite loops, or `ConcurrentModificationException`.

---

### Part 1 — Q2: Why is HATEOAS a hallmark of advanced REST design?

**HATEOAS** (Hypermedia As The Engine Of Application State) means that an API response includes links to related actions and resources, not just raw data. For example, a response from `GET /api/v1` includes:

```json
{
  "resources": {
    "rooms":   "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

This benefits client developers in several ways. First, **discoverability** — a client only needs to know the root URL; it can navigate the entire API from there by following links, rather than memorising every endpoint. Second, **loose coupling** — if the server renames a path (e.g. `/rooms` becomes `/campus-rooms`), clients that follow links automatically adapt, whereas clients hardcoding URLs break immediately. Third, **self-documenting** — the API communicates what actions are available in context (e.g. a sensor response could include a link to its readings), reducing dependence on external documentation. This makes the API far more resilient to change and easier for new developers to explore.

---

### Part 2 — Q1: IDs-only vs Full Objects in List Responses

Returning only IDs (e.g. `["LIB-301", "LAB-101"]`) uses minimal bandwidth but forces the client to make a separate `GET /{id}` request for every room it wants details about — this is the "N+1 requests" problem. For a campus with 500 rooms, that's 501 network round-trips to populate a single page.

Returning full objects (as implemented here) costs more bandwidth per response but lets the client render a complete list in a single request. For most use cases — dashboards, admin panels — this is preferable. The right choice depends on context: IDs-only suits scenarios where the client only needs to check existence, whereas full objects suit display-heavy use cases. A best-practice approach is to support both via query parameters (e.g. `?expand=true`).

---

### Part 2 — Q2: Is DELETE Idempotent?

Yes, `DELETE` is idempotent by the HTTP specification — sending the same DELETE request multiple times should have the same end result as sending it once.

In this implementation, the first `DELETE /api/v1/rooms/ENG-201` removes the room and returns `204 No Content`. Any subsequent identical DELETE request returns `404 Not Found` because the room is already gone. Critically, the **server state is the same** after both calls — the room does not exist either way. The response code differs (204 vs 404), but idempotency refers to the *effect on the server*, not the response code. This is fully compliant with REST idempotency semantics.

---

### Part 3 — Q1: Consequences of @Consumes Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that this endpoint only accepts requests with a `Content-Type: application/json` header. If a client sends data as `text/plain` or `application/xml`, JAX-RS will reject the request **before it even reaches the method body** and automatically return an `HTTP 415 Unsupported Media Type` response. No custom code is needed — the framework enforces the contract. This is important for data integrity: if the server expected JSON and received plain text, Jackson's deserializer would fail or produce a null/empty object, causing silent data corruption. The annotation prevents this at the HTTP layer cleanly.

---

### Part 3 — Q2: @QueryParam vs Path Segment for Filtering

Using `@QueryParam` for filtering (e.g. `GET /api/v1/sensors?type=CO2`) is preferable to embedding the filter in the path (e.g. `/api/v1/sensors/type/CO2`) for several reasons.

**Semantic correctness**: A path segment implies identifying a specific resource. `/sensors/type/CO2` incorrectly implies there is a unique resource *called* `type/CO2`, when in reality we are filtering a collection. Query parameters semantically communicate "optional search criteria applied to a collection."

**Optionality**: With `@QueryParam`, the parameter is optional by nature — `GET /api/v1/sensors` without any parameter still works and returns all sensors. With a path approach, you would need two separate method signatures to handle both cases.

**Composability**: Query parameters can be freely combined (e.g. `?type=CO2&status=ACTIVE`) without changing the URL structure. Adding a second filter to a path-based design requires defining additional nested path patterns.

**REST conventions**: Query strings are universally understood as filters and search parameters. This matches the expectations of experienced API consumers and follows industry standards like the Google API Design Guide.

---

### Part 4 — Q1: Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern allows a resource class to **delegate** handling of nested paths to a separate dedicated class, rather than cramming all logic into one file.

In this project, `SensorResource` handles `/api/v1/sensors/**` but delegates `/api/v1/sensors/{id}/readings/**` to `SensorReadingResource` via a locator method. This offers several architectural benefits:

**Separation of concerns**: `SensorResource` only deals with sensor management. `SensorReadingResource` only deals with reading history. Each class has a single, well-defined responsibility.

**Maintainability**: In a large API with dozens of nested paths, a single "mega-controller" becomes impossible to maintain. Sub-resources keep file sizes manageable and logic focused.

**Testability**: Each sub-resource class can be unit-tested in isolation without needing to instantiate the parent resource.

**Reusability**: A `SensorReadingResource` could theoretically be reused across multiple parent contexts if the design required it.

**Runtime flexibility**: JAX-RS resolves the sub-resource at runtime, which means the locator method can apply logic (e.g. validation, context injection) before handing off to the sub-resource.

---

### Part 5 — Q1: Why HTTP 422 is More Semantically Accurate than 404

When a client POSTs a new sensor with `"roomId": "FAKE-999"`, the issue is **not** that the endpoint `/api/v1/sensors` was not found (which is what `404 Not Found` communicates). The endpoint exists and is working correctly. The problem is that the *payload is semantically invalid* — it references a foreign key that does not resolve to an existing resource.

`HTTP 422 Unprocessable Entity` precisely communicates: "I understood your request, I can parse your JSON, but the *business logic* embedded in the payload is invalid." It separates the concern of "can I parse this?" (400 Bad Request) from "does this make sense in context?" (422). This gives API consumers clearer, more actionable error information — they know immediately that the issue is a broken reference in their data, not a malformed request or missing endpoint.

---

### Part 5 — Q2: Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a serious security vulnerability for several reasons:

**Information disclosure**: A stack trace reveals the internal package structure, class names, method names, and line numbers of the application (e.g. `com.smartcampus.storage.DataStore.getRoom(DataStore.java:47)`). An attacker uses this to map the application's architecture and identify attack surfaces.

**Technology fingerprinting**: Stack traces expose the exact versions of frameworks and libraries in use (e.g. Jersey 2.41, Jackson 2.14). Attackers can then look up known CVEs (Common Vulnerabilities and Exposures) for those specific versions and craft targeted exploits.

**Logic disclosure**: Exception messages often contain internal business logic details, SQL fragments, or file paths that reveal how the system works — information that should never leave the server.

**Assisted exploitation**: If an attacker can deliberately trigger exceptions (e.g. by sending malformed input), each stack trace is a free debugging session that helps them refine their attack.

The Global Exception Mapper in this project prevents all of this by catching every `Throwable`, logging the real error server-side (where only developers can see it), and returning only a generic `500 Internal Server Error` message to the client.

---

### Part 5 — Q3: Why Filters are Superior to Manual Logging

Using a JAX-RS filter (`@Provider` implementing `ContainerRequestFilter` / `ContainerResponseFilter`) to handle logging is an example of addressing a **cross-cutting concern** — functionality that applies uniformly across the entire application, independent of any specific business logic.

Inserting `Logger.info()` statements manually into every resource method has several problems. It requires repetitive boilerplate across every single method — if you have 15 endpoints, you write 15 log statements. If the logging format needs to change (e.g. adding a request ID), you must update every single method. It is easy to forget to add logging to a new endpoint. It mixes infrastructure concerns (logging) with business logic (creating a room), violating the Single Responsibility Principle.

A filter, by contrast, is declared once and automatically applied to **every** request and response across the entire API with zero changes to resource classes. This enforces consistency, simplifies maintenance, and keeps resource classes clean and focused purely on their business logic.


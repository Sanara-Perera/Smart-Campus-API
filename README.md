# Smart Campus Sensor & Room Management API

A fully-featured RESTful API built with **JAX-RS (Jersey)** and an embedded **Grizzly HTTP server** for the University of Westminster 5COSC022W Client-Server Architectures module.

The system provides a scalable backend for managing campus **Rooms** and their **IoT Sensors** (temperature monitors, CO2 sensors, occupancy trackers, etc.), including a historical log of sensor readings.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [How to Build and Run](#how-to-build-and-run)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Sample curl Commands](#sample-curl-commands)
7. [Conceptual Report — Question Answers](#conceptual-report--question-answers)

---

## API Design Overview

The API follows REST architectural principles and is structured around three core resources:

| Resource | Base Path | Description |
|---|---|---|
| Discovery | `GET /api/v1/` | API metadata and hypermedia links |
| Rooms | `/api/v1/rooms` | Campus room management |
| Sensors | `/api/v1/sensors` | IoT sensor registration and querying |
| Readings | `/api/v1/sensors/{id}/readings` | Historical reading logs (sub-resource) |

**Resource Hierarchy:**
```
/api/v1/
├── rooms/
│   ├── GET    (list all rooms)
│   ├── POST   (create room)
│   └── {roomId}/
│       ├── GET    (get room)
│       └── DELETE (delete room — blocked if sensors exist)
└── sensors/
    ├── GET    (list all, optional ?type= filter)
    ├── POST   (create sensor — validates roomId)
    └── {sensorId}/
        ├── GET    (get sensor)
        ├── DELETE (delete sensor)
        └── readings/
            ├── GET  (list reading history)
            ├── POST (add new reading — blocked if MAINTENANCE)
            └── {readingId}/
                └── GET (get one reading)
```

**Error Handling** is centralised through JAX-RS Exception Mappers. Every error returns a consistent JSON shape:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 cannot be deleted because it still has 2 sensor(s) assigned.",
  "path": "/api/v1/rooms/LIB-301"
}
```

---

## Technology Stack

| Component | Technology | Reason |
|---|---|---|
| Language | Java 11 | LTS version, widely supported |
| JAX-RS Implementation | Jersey 2.41 | Reference implementation of JAX-RS spec |
| HTTP Server | Grizzly (embedded) | Lightweight, no external server needed |
| JSON Serialisation | Jackson 2.15 | Industry-standard Java↔JSON converter |
| Build Tool | Maven 3.x | Dependency management and build automation |

> **No Spring Boot. No database. No ZIP files.** This project uses only JAX-RS as required by the coursework specification.

---

## Project Structure

```
smart-campus-api/
├── pom.xml                              ← Maven build configuration
├── README.md                            ← This file (report + instructions)
└── src/main/java/com/smartcampus/api/
    ├── Main.java                        ← Starts the embedded Grizzly server
    ├── SmartCampusApplication.java      ← JAX-RS Application config (@ApplicationPath)
    ├── model/
    │   ├── Room.java                    ← Room POJO (id, name, capacity, sensorIds)
    │   ├── Sensor.java                  ← Sensor POJO (id, type, status, currentValue, roomId)
    │   ├── SensorReading.java           ← SensorReading POJO (id, timestamp, value)
    │   └── ErrorResponse.java           ← Standard JSON error body
    ├── store/
    │   └── DataStore.java               ← Thread-safe singleton in-memory store (ConcurrentHashMap)
    ├── resource/
    │   ├── DiscoveryResource.java       ← GET /api/v1/ — HATEOAS discovery
    │   ├── RoomResource.java            ← /api/v1/rooms — full CRUD
    │   ├── SensorResource.java          ← /api/v1/sensors — CRUD + sub-resource locator
    │   └── SensorReadingResource.java   ← /api/v1/sensors/{id}/readings — sub-resource
    ├── exception/
    │   ├── ResourceNotFoundException.java         ← For 404 cases
    │   ├── RoomNotEmptyException.java             ← For 409 (room has sensors)
    │   ├── LinkedResourceNotFoundException.java   ← For 422 (bad roomId in sensor)
    │   └── SensorUnavailableException.java        ← For 403 (sensor in MAINTENANCE)
    ├── mapper/
    │   ├── ResourceNotFoundExceptionMapper.java        ← → HTTP 404
    │   ├── RoomNotEmptyExceptionMapper.java            ← → HTTP 409
    │   ├── LinkedResourceNotFoundExceptionMapper.java  ← → HTTP 422
    │   ├── SensorUnavailableExceptionMapper.java       ← → HTTP 403
    │   └── GlobalExceptionMapper.java                  ← → HTTP 500 (catch-all)
    └── filter/
        └── LoggingFilter.java           ← Logs all requests and responses
```

---

## How to Build and Run

### Prerequisites

Make sure you have the following installed before starting:

- **Java 11+** — check with `java -version`
- **Maven 3.6+** — check with `mvn -version`

If you do not have Java or Maven, install them:
- Java: https://adoptium.net/
- Maven: https://maven.apache.org/download.cgi

### Step 1 — Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the project

This compiles all Java files and packages them into a single runnable JAR:

```bash
mvn clean package
```

You should see `BUILD SUCCESS` at the end. This creates:
```
target/smart-campus-api-1.0.0.jar
```

### Step 3 — Run the server

```bash
java -jar target/smart-campus-api-1.0.0.jar
```

You will see output like:
```
INFO: Smart Campus API is running!
INFO: Base URL  : http://localhost:8080/api/v1
INFO: Press ENTER to stop the server...
```

### Step 4 — Test it is working

Open a new terminal window and run:

```bash
curl -X GET http://localhost:8080/api/v1/
```

You should receive a JSON response describing the API. The server is running correctly.

### Step 5 — Stop the server

Press **ENTER** in the terminal where the server is running.

> **Note:** The server uses in-memory storage. All data created during a session is lost when the server stops. Sample data (3 rooms, 5 sensors, 3 readings) is pre-loaded on every startup.

---

## API Endpoints Reference

### Discovery

| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | `/api/v1/` | API metadata and links | 200 |

### Rooms

| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get one room | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (fails if sensors exist) | 200 |

### Sensors

| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | `/api/v1/sensors` | List all sensors | 200 |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type | 200 |
| POST | `/api/v1/sensors` | Register a new sensor | 201 |
| GET | `/api/v1/sensors/{sensorId}` | Get one sensor | 200 |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor | 200 |

### Sensor Readings (Sub-Resource)

| Method | Path | Description | Success Code |
|--------|------|-------------|--------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | List all readings | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Record a new reading | 201 |
| GET | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Get one reading | 200 |

### Error Codes

| Code | Meaning | When |
|------|---------|------|
| 400 | Bad Request | Missing required fields |
| 403 | Forbidden | Sensor is in MAINTENANCE |
| 404 | Not Found | Resource ID does not exist |
| 409 | Conflict | Deleting a room that still has sensors |
| 415 | Unsupported Media Type | Client sent non-JSON content |
| 422 | Unprocessable Entity | Sensor references a non-existent roomId |
| 500 | Internal Server Error | Unexpected server error |

---

## Sample curl Commands

The following commands can be run from any terminal. The server must be running on `localhost:8080`.

### 1. Get API Discovery Info

```bash
curl -X GET http://localhost:8080/api/v1/ \
  -H "Accept: application/json"
```

**Expected response (200 OK):**
```json
{
  "name": "Smart Campus Sensor & Room Management API",
  "version": "1.0.0",
  "contact": "admin@smartcampus.ac.uk",
  "_links": {
    "rooms": "http://localhost:8080/api/v1/rooms",
    "sensors": "http://localhost:8080/api/v1/sensors"
  }
}
```

---

### 2. List All Rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms \
  -H "Accept: application/json"
```

**Expected response (200 OK):** Array of 3 pre-loaded rooms.

---

### 3. Create a New Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id": "SCI-205",
    "name": "Science Innovation Lab",
    "capacity": 40
  }'
```

**Expected response (201 Created):**
```json
{
  "id": "SCI-205",
  "name": "Science Innovation Lab",
  "capacity": 40,
  "sensorIds": []
}
```

---

### 4. Register a New Sensor (links it to a room)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id": "CO2-002",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "SCI-205"
  }'
```

**Expected response (201 Created):** The sensor object.

---

### 5. Filter Sensors by Type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

**Expected response (200 OK):** Only sensors with type "Temperature".

---

### 6. Add a Reading to a Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "value": 24.3
  }'
```

**Expected response (201 Created):** The reading object. The sensor's `currentValue` is now 24.3.

---

### 7. Get Reading History for a Sensor

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```

**Expected response (200 OK):** Array of all readings for TEMP-001.

---

### 8. Attempt to Delete a Room with Sensors (triggers 409)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Expected response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 cannot be deleted because it still has 2 sensor(s) assigned to it."
}
```

---

### 9. Register a Sensor with a Non-Existent Room (triggers 422)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id": "TEMP-999",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "FAKE-999"
  }'
```

**Expected response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot create sensor: the referenced Room with id 'FAKE-999' does not exist."
}
```

---

### 10. Post a Reading to a Sensor in MAINTENANCE (triggers 403)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{ "value": 15.0 }'
```

**Expected response (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor OCC-001 is currently in 'MAINTENANCE' state and cannot accept new readings."
}
```

---

## Conceptual Report — Question Answers

---

### Part 1, Question 1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every incoming HTTP request. This is known as the **request-scoped** (per-request) lifecycle. The JAX-RS runtime instantiates the class, handles the request, returns the response, and then the instance is discarded by the garbage collector.

**Architectural implication for in-memory data:** Because each resource instance is fresh per request, storing data as instance variables would mean that data is lost the moment the request ends. To maintain persistent in-memory state across requests, we use a **Singleton DataStore** — a class with exactly one instance for the entire lifetime of the server process (achieved via the "Initialization-on-demand holder" pattern).

**Concurrency and thread safety:** Multiple requests can arrive simultaneously, meaning multiple threads may access the DataStore at the same time. Using a plain `HashMap` or `ArrayList` under concurrent access leads to race conditions and data corruption. We therefore use `ConcurrentHashMap`, which uses internal lock striping to allow multiple threads to read simultaneously and safely handles concurrent writes without requiring explicit `synchronized` blocks. This prevents data loss and ensures the API behaves correctly under load.

---

### Part 1, Question 2 — HATEOAS and Hypermedia

**HATEOAS** (Hypermedia As The Engine Of Application State) is the principle that API responses should include hyperlinks to related resources, enabling clients to navigate the API dynamically rather than relying on hardcoded URL knowledge.

Our discovery endpoint (`GET /api/v1/`) demonstrates this by returning a `_links` object:
```json
{
  "_links": {
    "rooms":   "http://localhost:8080/api/v1/rooms",
    "sensors": "http://localhost:8080/api/v1/sensors"
  }
}
```

**Benefits over static documentation:**

1. **Self-documenting:** A new client developer can start at the root URL and discover the entire API by following links — similar to how a user navigates a website without needing a map.
2. **Reduced coupling:** Clients that follow links rather than hardcode URLs automatically adapt if the API restructures a path. No client code changes needed.
3. **State navigation:** In more advanced HATEOAS implementations, responses include only the links that are *currently valid* for the resource's state (e.g., a sensor in MAINTENANCE would not include a `post-reading` link), guiding clients toward valid actions only.

This is considered a hallmark of mature REST design (Level 3 in Richardson's REST Maturity Model).

---

### Part 2, Question 1 — Returning IDs vs Full Room Objects in Lists

When a client calls `GET /api/v1/rooms`, we can return either:

**Option A — IDs only:** `["LIB-301", "LAB-101", "HALL-001"]`
- Minimal bandwidth
- Client must then make N additional `GET /rooms/{id}` requests to get details
- This is the "N+1 request problem" — expensive in terms of latency and round trips

**Option B — Full objects (our implementation):**
```json
[{"id":"LIB-301","name":"Library Quiet Study","capacity":50,"sensorIds":[...]}, ...]
```
- Higher single-response payload size
- Client has everything it needs in one request — no follow-up calls needed
- Better for most real-world clients (dashboards, mobile apps) that display room details

**Our choice:** We return full objects because at campus scale (hundreds of rooms, not millions), the payload size is manageable, and the reduction in round-trips significantly improves client performance. For very large datasets, pagination (e.g., `?page=1&size=20`) would be introduced alongside full objects — not IDs.

---

### Part 2, Question 2 — Idempotency of DELETE

**Yes, DELETE is idempotent** in our implementation — but with an important nuance.

Idempotency means that making the same request multiple times produces the **same end state** on the server. It does NOT mean the HTTP response code will be identical each time.

In our implementation:
- **First DELETE** of room `SCI-205`: room is found and removed → **200 OK**
- **Second DELETE** of room `SCI-205`: room no longer exists → **404 Not Found**

The *server state* is identical after both calls (the room does not exist). This is correct and expected behaviour per the HTTP specification. The 404 on the second call is not a violation of idempotency — it simply acknowledges that the resource is already gone. A client that retries a DELETE after a network timeout (a common real-world scenario) can safely interpret either 200 or 404 as "the room is definitely gone."

This is why idempotent operations like DELETE are safe to retry — they will not accidentally create duplicate side effects.

---

### Part 3, Question 1 — @Consumes(APPLICATION_JSON) and Media Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that our POST method **only accepts** request bodies with `Content-Type: application/json`.

**What happens if a client sends `text/plain` or `application/xml`?**

JAX-RS intercepts the request **before it reaches our method** and automatically rejects it with:

```
HTTP 415 Unsupported Media Type
```

The error response is produced by the JAX-RS runtime itself — our resource method code never executes. This is a key benefit: we get automatic input format validation for free, without writing any explicit checking code.

This protects our API from:
- Accidentally trying to parse a plain text string as a Java `Sensor` object (which would throw a deserialization exception)
- Receiving XML when we've built a JSON-only service
- Clients with misconfigured HTTP clients sending wrong content types

The `@Produces(MediaType.APPLICATION_JSON)` annotation works in the opposite direction — it tells clients "I will only send back JSON." If a client sends `Accept: application/xml`, JAX-RS returns **406 Not Acceptable**.

---

### Part 3, Question 2 — @QueryParam vs Path Segment for Filtering

**Our implementation:** `GET /api/v1/sensors?type=CO2`
**Alternative:** `GET /api/v1/sensors/type/CO2`

**Why query parameters are superior for filtering:**

| Concern | Query Parameter (`?type=CO2`) | Path Segment (`/type/CO2`) |
|---|---|---|
| **Optionality** | Optional — `/sensors` works without filter | Required — `/sensors/type/` broken without value |
| **Semantic accuracy** | Filtering a collection, not identifying a resource | Implies CO2 is a unique resource, which it isn't |
| **Composability** | `?type=CO2&status=ACTIVE` — multiple filters trivially | `/type/CO2/status/ACTIVE` — messy, ambiguous order |
| **REST conventions** | Universally accepted idiom for search/filter | Confuses the path hierarchy |
| **Caching** | Query params are part of the cache key | Path segments are also part of the cache key (no practical difference here) |

The core REST principle is that URL **path segments** identify *specific resources* (e.g., `/rooms/LIB-301` identifies a unique room). **Query parameters** modify or filter the collection — they are "adjectives" on the request. CO2 is a filter criterion, not an identifiable resource, so it belongs in a query parameter.

---

### Part 4, Question 1 — Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern (used in `SensorResource.getReadingResource()`) delegates a portion of the URL hierarchy to a separate, dedicated resource class.

**Benefits:**

1. **Single Responsibility Principle:** `SensorResource` is responsible for sensor CRUD. It should not also be responsible for reading history management. `SensorReadingResource` owns that concern entirely.

2. **Manageability at scale:** If we defined every nested path (`/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`) inside one massive controller class, it would quickly grow to hundreds of lines and become very difficult to navigate, debug, and maintain.

3. **Independent testability:** `SensorReadingResource` can be unit tested in isolation by constructing it directly with a known `sensorId` — no need to wire up the full parent resource.

4. **Context encapsulation:** The sub-resource receives the `sensorId` through its constructor. Every method inside it then operates within that sensor's context automatically, without needing to look it up again.

5. **Reusability:** If another future resource type (e.g., `ExternalSensorResource`) also needed reading history, it could return the same `SensorReadingResource` from its own locator — zero code duplication.

---

### Part 5, Question 1 — Why HTTP 422 over 404 for Missing roomId Reference

When a client POSTs a new sensor with `"roomId": "FAKE-999"` and that room does not exist:

- **404 Not Found** would mean: "The URL you requested (`/api/v1/sensors`) does not exist." — This is **factually wrong**. The URL `/api/v1/sensors` is perfectly valid and exists.

- **422 Unprocessable Entity** means: "Your request was received, the URL is valid, the JSON is syntactically correct, BUT the data inside it has a semantic/business logic problem."

The **422** is semantically accurate because:
- The HTTP method is correct (POST)
- The URL exists and is reachable
- The JSON is well-formed and parseable
- The problem is a **broken reference inside the payload** — the `roomId` field points to a room that doesn't exist in the system (a referential integrity violation)

Using 404 here would be misleading to clients — they would think the endpoint itself is missing rather than understanding there is a data dependency problem in their request body. 422 precisely communicates "fix your data."

---

### Part 5, Question 2 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a **serious cybersecurity vulnerability**. An attacker can gather the following intelligence from a stack trace:

1. **Technology fingerprinting:** The full package names reveal the exact frameworks and libraries in use (e.g., `org.glassfish.jersey:2.41`, `com.fasterxml.jackson:2.15`). The attacker can then look up known CVEs (Common Vulnerabilities and Exposures) for those exact versions.

2. **Internal code structure:** Class names, method names, and line numbers reveal the application's internal architecture — which classes exist, how they relate, and where logic lives.

3. **File paths:** Absolute file paths in stack traces reveal server directory structure (`/home/ubuntu/apps/smart-campus/...`), operating system type, deployment conventions, and sometimes usernames.

4. **Business logic clues:** Method names like `deleteRoomIfEmpty()` or `validateSensorBeforeSave()` expose business rules the attacker can deliberately target or exploit.

5. **Targeted exception attacks:** Knowing which exception type was thrown and from which line, an attacker can craft specific inputs designed to repeatedly trigger that code path, potentially causing resource exhaustion (DoS) or bypassing logic.

**Our defence:** The `GlobalExceptionMapper` catches all `Throwable` exceptions, logs the full stack trace **server-side** (for developer debugging), and returns only a generic `"An unexpected error occurred"` message to the client. The client gets zero internal information.

---

### Part 5, Question 3 — Filters vs Manual Logging in Resource Methods

**The problem with manual logging:** Inserting `Logger.info(...)` calls directly into each resource method means:
- The same boilerplate code is duplicated across every endpoint (violates the DRY principle — Don't Repeat Yourself)
- A new developer adding a new endpoint might forget to add logging
- Changing the log format requires editing every single resource method
- Business logic and infrastructure concerns (logging) are mixed in the same method

**Why JAX-RS filters are better:**

1. **Single point of control:** Our `LoggingFilter` is one class. Changing the log format, adding timestamps, or disabling logging means editing one file.

2. **Automatic coverage:** The filter is applied to **every** request and response by the JAX-RS framework — it is physically impossible to forget to log a new endpoint.

3. **Separation of concerns:** Resource methods focus purely on business logic. The filter handles the cross-cutting concern of observability. This makes resource methods cleaner and easier to read.

4. **Composability:** We could add more filters (authentication, rate limiting, CORS headers) independently, each as a separate class, without touching the resource methods at all.

5. **Consistent log structure:** Every log line produced by `LoggingFilter` has the same format (`[REQUEST] --> GET /api/v1/rooms`), making log analysis and monitoring tools easy to configure.

This is the same principle that drives middleware in Express.js, interceptors in Spring, or decorators in Python frameworks — keep cross-cutting concerns separate from business logic.

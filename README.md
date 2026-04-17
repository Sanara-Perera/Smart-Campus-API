<div align="center">
<img src="banner.svg" width="100%" alt="Smart Campus API Banner"/>
</div>

<div align="center">
    
# 🏛️ Campus Sensor & Room Management API

### *A production-grade RESTful API powering the university's intelligent building infrastructure*

---

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JAX-RS](https://img.shields.io/badge/JAX--RS-Jersey%202.41-4B8BBE?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Grizzly](https://img.shields.io/badge/Server-Grizzly%20HTTP-00C7B7?style=for-the-badge&logo=server&logoColor=white)
![Jackson](https://img.shields.io/badge/JSON-Jackson-green?style=for-the-badge&logo=json&logoColor=white)
![License](https://img.shields.io/badge/License-Academic-purple?style=for-the-badge)

---

**Module:** 5COSC022W - Client-Server Architectures &nbsp;|&nbsp; **University of Westminster**  
**Weight:** 60% of Final Grade &nbsp;|&nbsp; **Deadline:** 24th April 2026

</div>

---

## 🌐 What is this?

You have been appointed as the **Lead Backend Architect** for the university's *Smart Campus* initiative. What began as a pilot project tracking individual temperature sensors has evolved into a **comprehensive campus-wide infrastructure system**.

This API manages **thousands of Rooms** and the diverse array of **Sensors** deployed within them — CO2 monitors, occupancy trackers, smart lighting controllers — providing a seamless interface for campus facilities managers and automated building systems.

---

## ⚡ Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/YOUR-USERNAME/smart-campus-api.git
cd smart-campus-api

# 2. Build the project (downloads all dependencies automatically)
mvn clean package

# 3. Launch the server
java -jar target/smart-campus-api-1.0.0.jar

# 4. Confirm it is alive
curl http://localhost:8080/api/v1/
```

> 🟢 **Server starts on** `http://localhost:8080/api/v1` — no external server required.  
> 📦 **Pre-loaded with** 3 rooms, 5 sensors, and 3 sensor readings on every startup.

---

## 🗺️ API Architecture

```
http://localhost:8080/api/v1/
│
├── 🔍  GET  /                          → Discovery & HATEOAS links
│
├── 🏠  /rooms
│   ├── GET    /                        → List all rooms
│   ├── POST   /                        → Create a new room
│   ├── GET    /{roomId}                → Get room details
│   └── DELETE /{roomId}                → Delete room (blocked if sensors exist)
│
└── 📡  /sensors
    ├── GET    /                        → List all sensors
    ├── GET    /?type=CO2               → Filter sensors by type
    ├── POST   /                        → Register new sensor (validates roomId)
    ├── GET    /{sensorId}              → Get sensor details
    ├── DELETE /{sensorId}              → Remove sensor
    └── 📊  /{sensorId}/readings
        ├── GET  /                      → Full reading history
        ├── POST /                      → Record new measurement
        └── GET  /{readingId}           → Get single reading
```

---

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| 🔵 Language | Java 17 | Core application logic |
| 🟠 JAX-RS | Jersey 2.41 | REST framework (reference implementation) |
| 🟢 HTTP Server | Grizzly (embedded) | Lightweight server, zero configuration |
| 🟡 JSON | Jackson 2.15 | Java ↔ JSON serialisation |
| 🔴 Build | Apache Maven 3.9 | Dependency management and packaging |
| ⚪ Storage | ConcurrentHashMap | Thread-safe in-memory data store |

> ❌ **No Spring Boot.** ❌ **No Database.** ❌ **No external server.**  
> ✅ Pure JAX-RS, exactly as required by the module specification.

---

## 📁 Project Structure

```
smart-campus-api/
├── 📄 pom.xml                          ← Maven build & dependency config
├── 📄 README.md                        ← You are here
└── src/main/java/com/smartcampus/api/
    │
    ├── 🚀 Main.java                    ← Entry point — starts Grizzly server
    ├── ⚙️  SmartCampusApplication.java  ← @ApplicationPath("/api/v1") config
    │
    ├── 📦 model/
    │   ├── Room.java                   ← id, name, capacity, sensorIds
    │   ├── Sensor.java                 ← id, type, status, currentValue, roomId
    │   ├── SensorReading.java          ← id, timestamp, value
    │   └── ErrorResponse.java          ← Standard JSON error shape
    │
    ├── 🗄️  store/
    │   └── DataStore.java              ← Singleton ConcurrentHashMap store
    │
    ├── 🌐 resource/
    │   ├── DiscoveryResource.java      ← GET /api/v1/ — HATEOAS discovery
    │   ├── RoomResource.java           ← Full room CRUD
    │   ├── SensorResource.java         ← Sensor CRUD + sub-resource locator
    │   └── SensorReadingResource.java  ← Reading history sub-resource
    │
    ├── ⚠️  exception/
    │   ├── ResourceNotFoundException.java        ← Triggers HTTP 404
    │   ├── RoomNotEmptyException.java             ← Triggers HTTP 409
    │   ├── LinkedResourceNotFoundException.java   ← Triggers HTTP 422
    │   └── SensorUnavailableException.java        ← Triggers HTTP 403
    │
    ├── 🗺️  mapper/
    │   ├── ResourceNotFoundExceptionMapper.java   ← → 404 Not Found
    │   ├── RoomNotEmptyExceptionMapper.java       ← → 409 Conflict
    │   ├── LinkedResourceNotFoundExceptionMapper  ← → 422 Unprocessable
    │   ├── SensorUnavailableExceptionMapper.java  ← → 403 Forbidden
    │   └── GlobalExceptionMapper.java             ← → 500 Safety net
    │
    └── 🔍 filter/
        └── LoggingFilter.java          ← Logs every request and response
```

---

## 🧪 Sample curl Commands

> Make sure the server is running before executing these commands.

### 🔍 1 — API Discovery
```bash
curl -X GET http://localhost:8080/api/v1/
```
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

### 🏠 2 — Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"SCI-205","name":"Science Innovation Lab","capacity":40}'
```
```json
{ "id": "SCI-205", "name": "Science Innovation Lab", "capacity": 40, "sensorIds": [] }
```

---

### 📡 3 — Register a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-002","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"SCI-205"}'
```

---

### 🔎 4 — Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

---

### 📊 5 — Record a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 24.3}'
```

---

### ❌ 6 — Delete Room with Sensors (409 Conflict)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 cannot be deleted because it still has 2 sensor(s) assigned to it."
}
```

---

### 🚫 7 — Sensor with Invalid Room Reference (422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"X-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"FAKE-999"}'
```
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot create sensor: the referenced Room with id 'FAKE-999' does not exist."
}
```

---

### 🔧 8 — Post Reading to MAINTENANCE Sensor (403)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 15.0}'
```
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor OCC-001 is currently in 'MAINTENANCE' state and cannot accept new readings."
}
```

---

## 📋 Error Response Reference

Every error in this API returns a consistent JSON structure. No raw stack traces are ever exposed to the client.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room with id 'LIB-999' was not found.",
  "path": "/api/v1/rooms/LIB-999"
}
```

| Status | Error | Scenario |
|--------|-------|----------|
| `400` | Bad Request | Missing required fields in request body |
| `403` | Forbidden | Posting a reading to a sensor in MAINTENANCE |
| `404` | Not Found | Resource ID does not exist |
| `409` | Conflict | Deleting a room that still has sensors |
| `415` | Unsupported Media Type | Client sent non-JSON content |
| `422` | Unprocessable Entity | Sensor references a non-existent roomId |
| `500` | Internal Server Error | Unexpected server error (safely masked) |

---

## 📝 Conceptual Report — Question Answers

---

### Part 1, Q1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every incoming HTTP request — this is the **request-scoped lifecycle**. The instance is created, used to handle the request, and then discarded by the garbage collector.

**Impact on data management:** Because each resource instance is fresh per request, storing rooms or sensors as instance variables would mean all data is lost after every request. To solve this, we use a **Singleton DataStore** — one shared instance for the entire server lifetime, implemented using the "Initialization-on-demand holder" pattern which is inherently thread-safe in Java.

**Concurrency and thread safety:** Multiple HTTP requests arrive simultaneously on different threads. A plain `HashMap` under concurrent access causes race conditions and data corruption. We use `ConcurrentHashMap` which handles concurrent reads and writes safely through internal lock striping — no manual `synchronized` blocks needed, and no risk of data loss under load.

---

### Part 1, Q2 — HATEOAS and Hypermedia

**HATEOAS** (Hypermedia As The Engine Of Application State) means that API responses embed hyperlinks to related resources, allowing clients to navigate the API dynamically rather than relying on hardcoded URL knowledge.

Our discovery endpoint returns a `_links` object containing URLs for all primary resource collections. **Benefits over static documentation:**

1. **Self-documenting** — a new client can start at the root URL and discover all available resources by following links, just like a browser navigates a website without needing a sitemap.
2. **Reduced coupling** — clients that follow links rather than hardcode URLs automatically adapt if path structures change, with no client-side code changes needed.
3. **State-driven navigation** — advanced HATEOAS implementations include only links that are valid for the resource's current state, guiding clients toward valid operations only.

This represents Level 3 (the highest level) of Richardson's REST Maturity Model.

---

### Part 2, Q1 — Returning IDs vs Full Room Objects

**IDs only** (`["LIB-301", "LAB-101"]`): minimal bandwidth, but forces the client to make N additional `GET /rooms/{id}` requests — the "N+1 request problem" — wasting time and network round-trips.

**Full objects (our choice):** Higher single-response payload, but the client has everything it needs in one request. At campus scale (hundreds of rooms), the payload size is manageable and the reduction in round-trips significantly improves client performance. For datasets of millions of records, pagination alongside full objects would be introduced.

---

### Part 2, Q2 — Idempotency of DELETE

**Yes, DELETE is idempotent** — but idempotency refers to the server state, not the response code.

- **First DELETE** of `LIB-301` → room removed → **200 OK**
- **Second DELETE** of `LIB-301` → already gone → **404 Not Found**

The end server state is identical after both calls (the room does not exist). The different response codes are correct and expected per the HTTP specification. A client retrying a DELETE after a network timeout can safely interpret both 200 and 404 as confirmation that the room is gone — no accidental duplicate deletions are possible.

---

### Part 3, Q1 — @Consumes(APPLICATION_JSON) and Media Type Mismatches

`@Consumes(MediaType.APPLICATION_JSON)` declares that our POST method only accepts requests with `Content-Type: application/json`. If a client sends `text/plain` or `application/xml`, JAX-RS intercepts the request **before it reaches our method** and automatically returns `HTTP 415 Unsupported Media Type`. Our resource method code never executes, protecting us from attempting to deserialise incompatible formats. The mirror annotation `@Produces(APPLICATION_JSON)` works in reverse — if a client sends `Accept: application/xml`, JAX-RS returns **406 Not Acceptable**.

---

### Part 3, Q2 — @QueryParam vs Path Segment for Filtering

**Our approach:** `GET /sensors?type=CO2` — **Alternative:** `GET /sensors/type/CO2`

| Concern | Query Parameter | Path Segment |
|---|---|---|
| Optionality | `/sensors` works without filter | `/sensors/type/` is broken without value |
| Semantic accuracy | Filtering a collection | Implies CO2 is a unique resource |
| Composability | `?type=CO2&status=ACTIVE` | `/type/CO2/status/ACTIVE` — unreadable |
| REST convention | Industry-standard for search/filter | Breaks path hierarchy semantics |

The REST principle is that path segments *identify resources* (`/rooms/LIB-301`). Query parameters *filter or modify* a collection. CO2 is a filter criterion, not a resource — it belongs in a query parameter.

---

### Part 4, Q1 — Benefits of the Sub-Resource Locator Pattern

The sub-resource locator in `SensorResource.getReadingResource()` delegates the `/readings` path hierarchy to a dedicated `SensorReadingResource` class.

**Benefits:**
1. **Single Responsibility** — `SensorResource` manages sensors; reading history is a separate concern owned by its own dedicated class.
2. **Manageability** — defining every nested path inside one massive controller produces hundreds of lines that become very difficult to navigate and debug at scale.
3. **Independent testability** — `SensorReadingResource` can be unit tested by constructing it directly with a known `sensorId`, with no parent wiring required.
4. **Context encapsulation** — the `sensorId` is passed through the constructor, so every method in the sub-resource operates in that sensor's context automatically.
5. **Reusability** — the sub-resource could be returned by multiple parent locators with zero code duplication.

---

### Part 5, Q1 — Why HTTP 422 over 404 for Missing roomId Reference

When a sensor POST body contains `"roomId": "FAKE-999"` and that room does not exist:

- **404** means: "The URL you requested does not exist." — Factually wrong. `/api/v1/sensors` is a perfectly valid URL.
- **422 Unprocessable Entity** means: "Your request arrived correctly, the URL is valid, the JSON is syntactically correct, but the data contains a semantic problem."

The problem is a **broken reference inside the payload** — a referential integrity violation. Using 422 precisely communicates "fix your data, not your URL," which is far more useful to the client developer than a misleading 404.

---

### Part 5, Q2 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a serious cybersecurity vulnerability. An attacker can gather:

1. **Technology fingerprinting** — exact package names reveal frameworks and versions (e.g., `jersey:2.41`), which can be cross-referenced with public CVE databases to find known exploits.
2. **Internal code structure** — class names, method names, and line numbers reveal the application's internal architecture.
3. **File paths** — absolute paths expose server directory structure, OS type, and sometimes usernames.
4. **Business logic clues** — method names like `validateSensorBeforeSave()` reveal rules that can be deliberately targeted or bypassed.
5. **Targeted exception attacks** — knowing which exception was thrown and from which line, an attacker crafts inputs to repeatedly trigger that code path, potentially causing denial of service.

**Our defence:** `GlobalExceptionMapper` catches all `Throwable` exceptions, logs the full stack trace server-side only, and returns a generic safe message to the client — zero internal information leaked.

---

### Part 5, Q3 — Filters vs Manual Logging in Resource Methods

**The problem with manual logging:** Inserting `Logger.info()` into every resource method duplicates the same boilerplate across every endpoint (violates DRY — Don't Repeat Yourself), risks being forgotten in new methods, and mixes infrastructure concerns with business logic.

**Why JAX-RS filters are superior:**

1. **Single point of control** — `LoggingFilter` is one class. Changing the log format means editing one file, not dozens.
2. **Automatic coverage** — the filter is applied to every request and response by the framework. It is physically impossible to forget to log a new endpoint.
3. **Separation of concerns** — resource methods focus purely on business logic; the filter handles observability independently.
4. **Composability** — additional filters (authentication, rate limiting, CORS headers) can be added as separate classes without touching any resource method.
5. **Consistent log structure** — every log line has the same format (`[REQUEST] --> GET /api/v1/rooms`), making log monitoring and analysis tools simple to configure.

This is the same principle behind middleware in Express.js, interceptors in Spring, and decorators in Python — keep cross-cutting concerns separate from business logic.

---

<div align="center">

---

```
Built with ☕ Java  |  🏛️ University of Westminster  |  5COSC022W Client-Server Architectures
```

![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)
![REST](https://img.shields.io/badge/Architecture-REST-blue?style=flat-square)
![Storage](https://img.shields.io/badge/Storage-In--Memory-orange?style=flat-square)
![No DB](https://img.shields.io/badge/Database-None-red?style=flat-square)

</div>

# Smart Campus — Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey 3)** and **Grizzly HTTP server** for managing rooms and sensors across a university smart campus. All data is stored fully in-memory using thread-safe `ConcurrentHashMap` structures — no database is used.

---

## API Overview

The API is versioned and served at: `http://localhost:8080/api/v1`

### Resources

| Resource | Base Path | Description |
|----------|-----------|-------------|
| Discovery | `GET /api/v1` | Lists all available resource endpoints and API metadata |
| Rooms | `/api/v1/rooms` | Create, retrieve, and delete campus rooms |
| Sensors | `/api/v1/sensors` | Register sensors linked to rooms; filter by type |
| Readings | `/api/v1/sensors/{sensorId}/readings` | Post and retrieve historical sensor readings |


---

## How to Build & Run

### Prerequisites

- Java 17+ (tested on Java 25)
- Maven 3.8+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/SmartCampusSensorAndRoomManagementAPI.git
cd SmartCampusSensorAndRoomManagementAPI

# 2. Compile the project
mvn compile

# 3. Start the server
mvn exec:java

# The server starts at: http://localhost:8080/api/v1
# Press ENTER in the terminal to stop the server.
```

---

## Sample curl Commands

### 1. Get API discovery info
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Get all rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Create a sensor linked to a room
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":400.0,"roomId":"LIB-301"}'
```

### 5. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 6. Post a new reading for a sensor (also updates sensor's currentValue)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":415.5}'
```

### 7. Attempt to delete a room that still has sensors (returns 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 8. Register a sensor with a non-existent roomId (returns 422 Unprocessable Entity)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":22.0,"roomId":"FAKE-999"}'
```

---

# Conceptual Report & Justifications

## Part 1: Service Architecture & Setup

### Question 1

**Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

### Answer

By default, a JAX-RS Resource class is **request-scoped**, meaning a **new instance** is created for each incoming HTTP request.

This supports stateless design because the object only exists for one request-response cycle. Therefore, data that must persist across requests should not be stored in instance variables.

In this coursework, shared data is stored in a dedicated `DataStore` class using thread-safe collections such as `ConcurrentHashMap`.

Benefits:

* Persistent in-memory data during server runtime
* Safe concurrent access
* Reduced race conditions
* No accidental data loss between requests

---

### Question 2

**Why is the provision of Hypermedia (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

### Answer

HATEOAS stands for **Hypermedia As The Engine Of Application State**.

It means the server includes navigational links inside responses so clients can discover available actions dynamically.

Example:

```json
{
  "rooms": "/api/v1/rooms",
  "sensors": "/api/v1/sensors"
}
```

Benefits:

* No hardcoded URLs
* Easier API navigation
* Reduced dependency on static documentation
* Better loose coupling between client and server
* Clients adapt more easily to endpoint changes

---

## Part 2: Room Management

### Question 1

**When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.**

### Answer

Returning only IDs reduces response size and saves bandwidth.

```json
["LIB-301", "LAB-201"]
```

However, clients need additional requests to fetch room details, creating the **N+1 request problem**.

Returning full room objects provides complete data immediately:

```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }
]
```

Trade-offs:

| IDs Only             | Full Objects   |
| -------------------- | -------------- |
| Smaller payload      | Larger payload |
| Faster transfer      | More data sent |
| More client requests | Fewer requests |

---

### Question 2

**Is the DELETE operation idempotent in your implementation? Provide a detailed justification.**

### Answer

Yes. `DELETE` is an **idempotent** HTTP method.

Example:

```http
DELETE /rooms/LIB-301
```

* First request → `204 No Content`
* Repeated request → `404 Not Found`

Although the status code may differ, the final system state remains the same: the room is deleted.

---

## Part 3: Sensor Operations & Linking

### Question 1

**Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

### Answer

The endpoint uses:

```java
@Consumes(MediaType.APPLICATION_JSON)
```

This means only JSON payloads are accepted.

If a client sends `text/plain` or `application/xml`, JAX-RS automatically rejects the request before business logic runs and returns:

```http
415 Unsupported Media Type
```

---

### Question 2

**You implemented this filtering using @QueryParam. Contrast this with an alterna-
**You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**

### Answer

Using path parameters for filtering creates unnecessary complexity.

Less ideal:

```http
/api/v1/sensors/type/CO2/status/ACTIVE
```

Preferred:

```http
/api/v1/sensors?type=CO2&status=ACTIVE
```

Benefits of query parameters:

* Better for optional filters
* Easier to combine conditions
* Cleaner URI structure
* Keeps `/sensors` as the main resource path
* More RESTful for collection searching

---

## Part 4: Deep Nesting with Sub-Resources

### Question

**Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?**

### Answer

The Sub-Resource Locator pattern allows a parent resource to delegate nested paths to dedicated child resources.

Example:

* `SensorResource` handles `/sensors`
* `SensorReadingResource` handles `/sensors/{id}/readings`

Benefits:

* Better separation of concerns
* Smaller and cleaner classes
* Easier maintenance
* Avoids “God Class” anti-pattern
* Better scalability for larger APIs

---

## Part 5: Error Handling, Mappers & Filters

### Question 1

**Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

### Answer

A `404 Not Found` means the requested URL does not exist.

In this case:

```http
POST /api/v1/sensors
```

The endpoint exists and the JSON syntax is valid. The problem is a linked field such as `roomId` references a room that does not exist.

Therefore, `422 Unprocessable Entity` is more accurate because semantic validation failed.

---

### Question 2

**From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

### Answer

Raw stack traces may expose:

* Package names
* Class names
* Framework details
* Dependency versions
* File paths
* Internal logic flow

Attackers can use this information to identify vulnerabilities and plan targeted attacks.

---

### Question 3

**Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**

### Answer

JAX-RS filters apply shared logic automatically to all requests and responses.

Examples:

* Logging
* Authentication
* Headers
* Metrics

Benefits:

* Follows DRY principle
* Keeps resource classes clean
* Consistent behavior across endpoints
* Easier maintenance
* New endpoints automatically inherit logging

Example:

```text
GET /api/v1/rooms
Response Status: 200
```

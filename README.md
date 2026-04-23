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
git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
cd smart-campus-api
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


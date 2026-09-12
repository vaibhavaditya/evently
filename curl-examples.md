# Evently Core Service API

Base URL:

http://localhost:8080

## 1. Create Event

Creates an event with status `DRAFT`.

```bash
curl -X POST http://localhost:8080/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Rock Concert",
    "organizerName": "ABC Events",
    "organizerMobile": "9876543210",
    "city": "Delhi",
    "category": "MUSIC"
  }'
```

Expected:

```text
201 Created
```

Save the returned event ID for the following requests.

---

## 2. Get Event By ID

Replace `<event-id>` with an existing event ID.

```bash
curl http://localhost:8080/v1/events/<event-id>
```

Expected:

```text
200 OK
```

---

## 3. List Events

Get all events:

```bash
curl http://localhost:8080/v1/events
```

### Filter by City

```bash
curl "http://localhost:8080/v1/events?city=Delhi"
```

### Filter by Category

```bash
curl "http://localhost:8080/v1/events?category=MUSIC"
```

### Filter by Status

```bash
curl "http://localhost:8080/v1/events?status=DRAFT"
```

### Pagination

```bash
curl "http://localhost:8080/v1/events?page=0&size=10"
```

### Multiple Filters

```bash
curl "http://localhost:8080/v1/events?city=Delhi&category=MUSIC&status=DRAFT&page=0&size=10"
```

All filters are optional.

---

## 4. Update Event Status

### DRAFT → PUBLISHED

Replace `<event-id>` with an event currently in `DRAFT`.

```bash
curl -X PATCH http://localhost:8080/v1/events/<event-id>/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PUBLISHED"
  }'
```

Expected:

```text
200 OK
```

### PUBLISHED → CANCELLED

Replace `<event-id>` with an event currently in `PUBLISHED`.

```bash
curl -X PATCH http://localhost:8080/v1/events/<event-id>/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CANCELLED"
  }'
```

Expected:

```text
200 OK
```

### PUBLISHED → SOLD_OUT

Replace `<event-id>` with an event currently in `PUBLISHED`.

```bash
curl -X PATCH http://localhost:8080/v1/events/<event-id>/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SOLD_OUT"
  }'
```

Expected:

```text
200 OK
```

---

## 5. Event Statistics

```bash
curl http://localhost:8080/v1/events/stats
```

Expected:

```text
200 OK
```

---

# Error Cases

## 6. Duplicate Organizer Mobile

Create an event using a mobile number that already exists.

```bash
curl -X POST http://localhost:8080/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Another Concert",
    "organizerName": "XYZ Events",
    "organizerMobile": "9876543210",
    "city": "Mumbai",
    "category": "MUSIC"
  }'
```

Expected:

```text
409 Conflict
```

The response should contain the proper error envelope.

---

## 7. Invalid Status Transition

An event in `DRAFT` cannot directly transition to `CANCELLED`.

Replace `<event-id>` with an event currently in `DRAFT`.

```bash
curl -X PATCH http://localhost:8080/v1/events/<event-id>/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CANCELLED"
  }'
```

Expected:

```text
400 Bad Request
```

Valid transitions are:

```text
DRAFT → PUBLISHED

PUBLISHED → CANCELLED

PUBLISHED → SOLD_OUT
```

---

## 8. Event Not Found

Use an ID that does not exist.

```bash
curl http://localhost:8080/v1/events/00000000-0000-0000-0000-000000000000
```

Expected:

```text
404 Not Found
```

The response should contain the proper error envelope.



## Phase 2
# Evently API - cURL Examples

All requests are sent to the **BFF**.

BFF URL:

```text
http://localhost:8082
```

The client does not directly call `evt-open-service` or `evt-core-service`.

---

## 1. Create Event

```bash
curl -X POST http://localhost:8082/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Rock Concert",
    "organizerName": "ABC Events",
    "organizerMobile": "9876543210",
    "city": "Delhi",
    "category": "MUSIC"
  }'
```

---

## 2. Get Event

Replace `<event-id>` with the event ID.

```bash
curl http://localhost:8082/api/v1/events/<event-id>
```

---

## 3. List Events

### Get all events

```bash
curl http://localhost:8082/api/v1/events
```

### Filter by city

```bash
curl "http://localhost:8082/api/v1/events?city=Delhi"
```

### Filter by category

```bash
curl "http://localhost:8082/api/v1/events?category=MUSIC"
```

### Filter by status

```bash
curl "http://localhost:8082/api/v1/events?status=PUBLISHED"
```

### Pagination

```bash
curl "http://localhost:8082/api/v1/events?page=0&size=10"
```

---

## 4. Update Event Status

Replace `<event-id>` with the event ID.

```bash
curl -X PATCH http://localhost:8082/api/v1/events/<event-id>/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PUBLISHED"
  }'
```

---

## 5. Event Statistics

```bash
curl http://localhost:8082/api/v1/events/stats
```

---

## Request Flow

All external requests enter through the BFF:

```text
Client
   |
   | HTTP
   v
BFF :8082
   |
   | HTTP / Feign
   v
Open Service :8081
   |
   | gRPC
   v
Core Service :9090
   |
   v
PostgreSQL
```
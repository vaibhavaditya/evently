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
---

# Phase 3 — Kafka + Notification Service + MongoDB

## 1. Create Event 1 — Mumbai

```bash
curl -X POST http://localhost:8082/api/v1/events \
-H "Content-Type: application/json" \
-d '{
  "eventName": "Mumbai Music Fest",
  "organizerName": "Evently Music",
  "organizerMobile": "9876543213",
  "city": "Mumbai",
  "category": "MUSIC"
}'
```

The event is created through the BFF and stored in PostgreSQL.
After successful creation, `evt-open-service` publishes an
`EVENT_PUBLISHED` message to Kafka.

---

## 2. Create Event 2 — Mumbai

```bash
curl -X POST http://localhost:8082/api/v1/events \
-H "Content-Type: application/json" \
-d '{
  "eventName": "Mumbai Comedy Night",
  "organizerName": "Comedy Events",
  "organizerMobile": "9876543214",
  "city": "Mumbai",
  "category": "COMEDY"
}'
```

---

## 3. Create Event 3 — Delhi

```bash
curl -X POST http://localhost:8082/api/v1/events \
-H "Content-Type: application/json" \
-d '{
  "eventName": "Delhi Sports Meet",
  "organizerName": "Delhi Sports",
  "organizerMobile": "9876543215",
  "city": "Delhi",
  "category": "SPORTS"
}'
```

---

## 4. Check Mumbai Dashboard

Wait a few seconds for Kafka to be consumed by the notification service.

```bash
curl http://localhost:8083/v1/dashboard/Mumbai
```

Expected result:

```json
{
  "city": "Mumbai",
  "totalEvents": 2,
  "publishedEvents": 2,
  "cancelledEvents": 0,
  "soldOutEvents": 0,
  "eventsByCategory": {
    "MUSIC": 1,
    "COMEDY": 1
  }
}
```

---

## 5. Check Delhi Dashboard

```bash
curl http://localhost:8083/v1/dashboard/Delhi
```

Expected result:

```json
{
  "city": "Delhi",
  "totalEvents": 1,
  "publishedEvents": 1,
  "cancelledEvents": 0,
  "soldOutEvents": 0,
  "eventsByCategory": {
    "SPORTS": 1
  }
}
```

The dashboard is updated asynchronously by `evt-notification-service`
after consuming the Kafka messages.

---

## 6. Get Notification

Use the `entityId` returned when creating an event.

```bash
curl "http://localhost:8083/v1/notifications?entityId=<event-id>"
```

The API returns the latest notification for that event.

---

## 7. DLT Test

The notification service retries failed Kafka messages and sends a
message to the Dead Letter Topic after the configured retry attempts.

DLT topic:

```text
event.published.dlt
```

To view messages in the DLT:

```bash
docker exec evently-kafka \
/opt/kafka/bin/kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic event.published.dlt \
--from-beginning
```

A malformed Kafka message is used to demonstrate the DLT.

After the retry attempts are exhausted, the malformed message appears
in `event.published.dlt`.

---

# Phase 3 Request Flow

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

Open Service :8081
   |
   | Kafka
   v
Kafka :9092
   |
   | @KafkaListener
   v
Notification Service :8083
   |
   +------> MongoDB
   |
   +------> CityDashboard
```

---

# Phase 3 Self-Check Questions

## 1. What happens if the consumer crashes after writing to MongoDB but before ACK?

Kafka provides at-least-once delivery.

If MongoDB is written successfully but the consumer crashes before
acknowledging the Kafka message, Kafka can deliver the same message
again after the consumer restarts.

The idempotency check prevents the duplicate from being inserted:

```java
if (repository.findByEventId(eventId).isPresent()) {
    return false;
}
```

Here, `eventId` uniquely identifies the Kafka message.

Our message also contains:

```text
eventId  = unique Kafka message ID
entityId = actual Event ID
```

Therefore, a redelivered Kafka message is detected and skipped.

---

## 2. Why is the dashboard a pre-computed document?

The notification service maintains a `CityDashboard` document for each
city.

For example:

```text
Mumbai
 ├── totalEvents: 2
 ├── publishedEvents: 2
 └── eventsByCategory
      ├── MUSIC: 1
      └── COMEDY: 1
```

When the dashboard is requested, MongoDB can directly retrieve the
document:

```java
repository.findById(city);
```

The application does not need to aggregate all notification documents
on every request.

With a large number of notification documents, performing an
aggregation every time a dashboard is requested would require more
database work and can become expensive as the data and request volume
grow.

The pre-computed dashboard moves this work to the Kafka consumer and
makes dashboard reads simpler and faster.

---

## 3. Why does the producer write the DB row before publishing to Kafka?

The event is first successfully stored in PostgreSQL.

Then the Kafka message is published.

```text
PostgreSQL
    |
    | successful write
    v
Kafka
    |
    v
Notification Service
    |
    v
MongoDB
```

If Kafka were published first and the database write failed:

```text
Kafka publish succeeds
        |
        v
Database write fails
```

the notification service could receive an event that was never
successfully stored in PostgreSQL.

This could create inconsistency between the source database and the
MongoDB read model.

There is still a possible failure if PostgreSQL succeeds but Kafka
publishing fails:

```text
PostgreSQL write succeeds
        |
        v
Kafka publish fails
```

The event exists in PostgreSQL, but downstream consumers have not
received the event.

A production solution for this type of failure is commonly the
transactional outbox pattern.
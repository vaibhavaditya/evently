# Evently

Evently is a microservices-based event ticketing backend built using Java and Spring Boot. The system allows organizers to manage events and provides city-wise event information. It consists of four independent services: `evt-bff` acts as the client-facing edge service and handles authentication and authorization, `evt-open-service` orchestrates requests between services, `evt-core-service` contains the core business logic and persists event data in PostgreSQL, and `evt-notification-service` consumes Kafka events and stores pre-computed dashboard data in MongoDB. The system also uses Redis for temporary data such as OTPs.

## Architecture

```text
Client / Postman
       |
       | REST + JSON
       | JWT
       v
+-------------------+
|      evt-bff      |
| JWT + Role Check  |
+---------+---------+
          |
          | REST
          | OpenFeign
          v
+----------------------+
|   evt-open-service   |
|     Orchestrator     |
+----------+-----------+
           |
           | gRPC
           v
+----------------------+
|   evt-core-service   |
|   Business Logic     |
|   Spring Data JPA    |
+----------+-----------+
           |
           v
      PostgreSQL


evt-open-service
        |
        | publish: event.published
        v
      Kafka
        |
        v
+--------------------------+
| evt-notification-service |
|      Kafka Consumer      |
+------------+-------------+
             |
             v
          MongoDB


Redis
  |
  +---- OTP / temporary data
```

## Services

- `evt-bff` — Client-facing REST API, JWT validation, role-based authorization, and Feign client.
- `evt-open-service` — Orchestrates operations, communicates with Core using gRPC, and publishes Kafka events.
- `evt-core-service` — Contains business logic and manages persistent event data using PostgreSQL.
- `evt-notification-service` — Consumes Kafka events and maintains read-side/dashboard documents in MongoDB.

## Infrastructure

The local infrastructure is managed using Docker Compose:

- PostgreSQL 16
- MongoDB 7
- Apache Kafka
- Redis

Start the infrastructure with:

```bash
docker compose up -d
```

Check container health with:

```bash
docker compose ps
```

Stop the infrastructure with:

```bash
docker compose down
```

To also remove persistent Docker volumes:

```bash
docker compose down -v
```

Phase 1: Event Core Service

The evt-core-service contains the core event
business logic and persists event data in PostgreSQL.

### Why does `localhost` not work inside a container but 
the service name `postgres` does?

Inside a Docker container, `localhost` refers to the container itself, 
not to other containers. Therefore, if `evt-core-service` uses 
`localhost:5432`, it looks for PostgreSQL inside the `evt-core-service` 
container, where PostgreSQL is not running.

Docker Compose creates a network for the services and provides 
DNS-based service discovery. The PostgreSQL service is named `postgres`
in `docker-compose.yml`, so `evt-core-service` can connect to 
PostgreSQL using:

`jdbc:postgresql://postgres:5432/evently`

Here, `postgres` resolves to the PostgreSQL container's network address.

evt-core-service container
|
| localhost:5432 ❌
| → looks inside itself
|
| postgres:5432 ✅
| → finds PostgreSQL container



### What does FetchType.LAZY do and when does it bite you?

`FetchType.LAZY` means that a related entity is not loaded from the 
database immediately when the parent entity is loaded. The related 
data is loaded only when it is actually accessed.

This can improve performance because unnecessary related data is not 
fetched.

However, it can cause problems when the related data is accessed 
after the Hibernate persistence context/session has already been 
closed. In that situation, Hibernate cannot load the relationship 
and may throw a `LazyInitializationException`.

For example, if an Event has a lazy relationship to another entity 
and the relationship is accessed after leaving the transactional 
context, the required data may no longer be available.

LAZY

Load Event
↓
Don't load related data yet
↓
event.getOrganizer()
↓
NOW load organizer




### What happens if two requests create an event with the same 
organizer mobile at the same time?

The application can perform a Java-level check such as 
`existsByOrganizerMobile()` before creating the event. However, this
check alone does not guarantee uniqueness when two requests arrive at
nearly the same time.

For example, both requests could check the database before either 
request has inserted the event:

Request A → mobile does not exist
Request B → mobile does not exist

Both requests could then attempt the insert.

The database UNIQUE constraint on `organizerMobile` is the actual 
protection against duplicate data. The database allows only one insert
and rejects the other with a unique-constraint violation.

Therefore, the Java check provides an early and user-friendly 
validation, but the database constraint provides the final guarantee
for uniqueness under concurrent requests.

The duplicate database exception should then be handled by the 
application's global exception handler and returned as a 
`409 Conflict` response.

Request A ──┐
├── Java exists check → doesn't exist
Request B ──┘

             ↓

       Both try INSERT
             ↓
       ┌─────────────┐
       │ PostgreSQL  │
       │ UNIQUE      │
       │ constraint  │
       └─────────────┘
          ↓       ↓
        A ✅      B ❌
                 409

## Phase 2 Self-Checks

### Why use gRPC between internal services but REST at the edge?

The BFF is the entry point for external clients, so it exposes REST APIs using
HTTP and JSON. REST is simple and widely supported by clients, browsers,
Postman, cURL, and mobile applications.

For internal service-to-service communication, `evt-open-service` communicates
with `evt-core-service` using gRPC and Protobuf. gRPC provides a strongly typed
contract and efficient binary communication.

The architecture is:

Client
|
| REST / HTTP + JSON
v
evt-bff
|
| REST / HTTP
v
evt-open-service
|
| gRPC / Protobuf
v
evt-core-service
|
v
PostgreSQL


### What breaks if the proto file adds a field?

The `.proto` file is used to generate the Java Protobuf and gRPC classes used
by the services.

If a field is added to the proto, the `event-grpc-contracts` module must be
rebuilt so that the generated classes contain the new field.

The services that depend directly on the generated gRPC contract,
`evt-open-service` and `evt-core-service`, then need to be rebuilt.

The BFF does not need to be rebuilt just because an internal gRPC field was
added because it communicates with `evt-open-service` through HTTP rather than
using the gRPC contract directly.

The shared `event-grpc-contracts` module provides a single source of truth for
the internal API contract. Both Open Service and Core Service use the same
generated classes, preventing the services from maintaining separate or
inconsistent proto definitions.


### If Open Service is down, what does the BFF return today?

When Open Service returns an HTTP error such as 400, 404, or 500, the Feign
`FeignErrorDecoder` converts the response into an `OpenServiceException`.
The BFF's `GlobalExceptionHandler` then returns the corresponding HTTP status.

However, if Open Service is completely down, there is no HTTP response to
decode. Feign encounters a connection failure, which is not currently handled
by our `OpenServiceException` handler. Therefore, the BFF can currently return
a generic 500 Internal Server Error.

Ideally, the BFF should return:

HTTP 503 Service Unavailable

with a response such as:

{
"success": false,
"message": "Event open service is currently unavailable"
}

A 503 is more appropriate because the BFF is available but one of its
downstream services is unavailable.

---

# Phase 3 — Kafka + Notification Service + MongoDB

## Kafka Flow

After a successful event creation or status update, `evt-open-service`
publishes an event to Kafka.

The flow is:

```text
evt-open-service
       |
       | Kafka
       v
Kafka
       |
       | @KafkaListener
       v
evt-notification-service
       |
       +------> EventNotification
       |
       +------> CityDashboard
                    |
                    v
                 MongoDB


# Kafka Consumer & Dashboard Design

## Your consumer crashes after writing to Mongo but before ACK. What happens on restart, and which line of your code saves you?

Kafka provides **at-least-once delivery**.

Suppose the following happens:

```text
Kafka sends message
       ↓
Notification service receives message
       ↓
MongoDB write succeeds
       ↓
Consumer crashes
       ↓
ACK was never sent
```

Because the message was not acknowledged, Kafka can **redeliver the message** after the consumer restarts.

Our **idempotency check** prevents the same Kafka message from being inserted again:

```kotlin
if (repository.findByEventId(eventId).isPresent()) {
    return false
}
```

The important line is:

```kotlin
repository.findByEventId(eventId)
```

It checks whether the Kafka message has already been processed.

In our message structure:

```text
eventId  = unique Kafka message ID
entityId = actual Event ID
```

Therefore, after redelivery:

```text
Redelivered Kafka message
        ↓
Check eventId
        ↓
eventId already exists in MongoDB
        ↓
Skip duplicate processing
```

This makes the consumer **idempotent**.

---

## Why is the dashboard a pre-computed document instead of a Mongo aggregation / GROUP BY at read time? When would the aggregation approach fall over?

The notification service maintains a pre-computed `CityDashboard` document for each city.

For example:

```text
Mumbai
 |
 +-- totalEvents: 2
 +-- publishedEvents: 2
 +-- cancelledEvents: 0
 +-- soldOutEvents: 0
 |
 +-- eventsByCategory
       |
       +-- MUSIC: 1
       +-- COMEDY: 1
```

When the client requests:

```http
GET /v1/dashboard/Mumbai
```

MongoDB can directly retrieve the document using:

```text
repository.findById(city);
```

There is no need to scan and aggregate all notification documents for every dashboard request.

The calculation happens when Kafka messages are consumed:

```text
Kafka message
     ↓
Notification Service
     ↓
Update CityDashboard
     ↓
MongoDB
```

### Why not aggregate at read time?

If we used MongoDB aggregation at read time, every dashboard request would need to perform operations such as:

- Filtering
- Grouping
- Counting
- Calculating category totals
- Calculating status totals

As the number of notification documents and dashboard requests grows, repeatedly performing those aggregations can increase:

- Database CPU usage
- Database I/O
- Query latency

The **pre-computed document** moves the calculation to the event-processing side and makes dashboard reads a simple:

```text
findById(city)
```

operation.

### Trade-off

The trade-off is that the dashboard must be updated correctly whenever relevant events are consumed.

In other words:

```text
Write/processing side
        ↓
More work
        ↓
Pre-computed dashboard
        ↓
Fast reads
```

This design is useful when dashboards are read frequently but the underlying event data changes less frequently than the dashboard is requested.

---

## Why does the producer write the DB row before publishing to Kafka, and what is the failure mode if you publish first?

PostgreSQL is the **source database** for events.

The intended sequence is:

```text
PostgreSQL
    ↓
Event successfully stored
    ↓
Kafka
    ↓
Notification Service
    ↓
MongoDB
```

The database write happens before Kafka publishing so Kafka does not announce an event that was never successfully stored in the source database.

### What happens if Kafka is published first?

Consider this sequence:

```text
Kafka publish succeeds
        ↓
PostgreSQL write fails
```

The notification service could consume and process an event that does not actually exist in PostgreSQL.

That could create inconsistency between:

```text
PostgreSQL = Source of Truth
MongoDB    = Read Model
```

For example:

```text
Kafka
 ↓
EVENT_PUBLISHED
 ↓
Notification Service
 ↓
MongoDB updated
```

while:

```text
PostgreSQL
 ↓
Event does not exist
```

The downstream system would therefore believe an event exists even though the source database does not contain it.

---

### What happens if PostgreSQL succeeds but Kafka fails?

There is also a failure in the opposite direction:

```text
PostgreSQL write succeeds
        ↓
Kafka publish fails
```

In this case:

```text
PostgreSQL
    ↓
Event exists
```

but:

```text
Kafka
    ↓
Event was never published
```

Therefore downstream consumers such as the notification service will not receive the event.

This creates another form of inconsistency:

```text
PostgreSQL
   |
   | Event exists
   ↓
Kafka
   |
   | Event missing
   ↓
MongoDB
   |
   | Dashboard not updated
```

---

## Transactional Outbox Pattern

A common production solution for this type of failure is the **Transactional Outbox Pattern**.

Instead of directly doing:

```text
Write PostgreSQL
       ↓
Publish Kafka
```

we write both the business data and an outbox event into PostgreSQL within the **same database transaction**:

```text
                    PostgreSQL
                        |
             ┌──────────┴──────────┐
             ↓                     ↓
       Event table           Outbox table
             |                     |
             └──────────┬──────────┘
                        |
                  Same transaction
                        |
                        ↓
                  COMMIT succeeds
                        |
                        ↓
              Outbox Publisher
                        |
                        ↓
                     Kafka
                        |
                        ↓
              Notification Service
                        |
                        ↓
                    MongoDB
```

For example:

```text
BEGIN TRANSACTION

INSERT INTO events (...)
INSERT INTO outbox_events (...)

COMMIT
```

Both records are committed together.

If the transaction fails:

```text
Event insert      ❌
Outbox insert     ❌
```

Neither record is committed.

If the transaction succeeds:

```text
Event insert      ✅
Outbox insert     ✅
```

The outbox publisher can later read the outbox table and publish the event to Kafka.

This prevents the situation where the event exists in PostgreSQL but the system completely loses the information that it needs to publish to Kafka.

### Key idea

```text
PostgreSQL transaction
        ↓
Event + Outbox record
        ↓
Reliable persistence
        ↓
Outbox Publisher
        ↓
Kafka
        ↓
Consumers
```

The **Transactional Outbox Pattern** therefore provides a reliable bridge between the PostgreSQL database and Kafka without requiring a distributed transaction between PostgreSQL and Kafka.
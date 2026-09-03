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
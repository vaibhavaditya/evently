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
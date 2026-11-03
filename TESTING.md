# Evently – Testing Documentation

## 1. Test Execution

All five Maven modules were tested independently using Maven.

```bash
mvn test
```

**Result:** BUILD SUCCESS in all five modules.

## 2. Test Inventory

| Module | Test Class | Behavior Tested | Test Type |
|---|---|---|---|
| evt-bff | EvtBffApplicationTests | Verifies Spring application context loads successfully | Integration |
| evt-bff | EventControllerTest | Event API endpoints, request validation, and HTTP responses | Slice |
| evt-bff | EventSecurityTest | Authentication and authorization rules | Slice |
| evt-bff | EventlyEndToEndTest | End-to-end event creation and notification workflow | Integration |
| evt-core-service | EvtCoreServiceApplicationTests | Verifies Spring application context loads successfully | Integration |
| evt-core-service | EventServiceImplTest | Event creation, duplicate organizer checks, and status transitions | Unit |
| evt-core-service | EventRepositoryTest | PostgreSQL persistence, filtering, pagination, and unique constraints | Integration |
| evt-open-service | EvtOpenServiceApplicationTests | Verifies Spring application context loads successfully | Integration |
| evt-notification-service | EvtNotificationServiceApplicationTests | Verifies Spring application context loads successfully | Integration |
| evt-notification-service | EventKafkaIntegrationTest | Kafka message consumption, MongoDB persistence, and idempotency | Integration |
| evt-grpc-contracts | No test classes | Contains gRPC/protobuf contracts | N/A |

## 3. Test Types

- **Unit tests:** Test individual classes or business logic in isolation, generally using mocks.
- **Slice tests:** Test a specific layer of the Spring application, such as MVC controllers or security configuration.
- **Integration tests:** Test interactions between multiple components or with external infrastructure such as PostgreSQL, Kafka, and MongoDB.

## 4. Code Coverage – JaCoCo

JaCoCo was configured to generate code coverage reports for the service modules.

| Module | Line Coverage |
|---|---:|
| evt-bff | 80% |
| evt-open-service | 11% |
| evt-core-service | 16% |
| evt-notification-service | 68% |
| evt-grpc-contracts | Not reported |

Coverage values are taken from the generated JaCoCo HTML reports.

## 5. Report Location

Each module's JaCoCo HTML report is generated at:

```text
target/site/jacoco/index.html
```

Open the HTML report to inspect line, instruction, branch, method, and class coverage.

## 6. Summary

- All five Maven modules successfully passed `mvn test`.
- JaCoCo coverage reports were generated for the service modules.
- Unit, slice, and integration testing are represented across the project.

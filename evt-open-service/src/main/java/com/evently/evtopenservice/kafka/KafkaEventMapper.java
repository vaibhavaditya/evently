package com.evently.evtopenservice.kafka;

import com.evently.grpc.Event;
import com.evently.grpc.EventCategory;
import com.evently.grpc.EventStatus;

import java.time.Instant;
import java.util.UUID;

public final class KafkaEventMapper {

    private KafkaEventMapper() {
    }

    public static EventMessage toMessage(
            Event event,
            EventType eventType
    ) {

        EventPayload payload = new EventPayload(
                event.getEventName(),
                event.getCity(),
                mapCategory(event.getCategory()),
                mapStatus(event.getStatus())
        );

        return new EventMessage(
                UUID.randomUUID().toString(),  // unique Kafka message ID
                event.getId(),                 // actual Event ID
                eventType.name(),
                Instant.now(),
                payload
        );
    }

    private static String mapCategory(EventCategory category) {

        return switch (category) {
            case MUSIC -> "MUSIC";
            case SPORTS -> "SPORTS";
            case COMEDY -> "COMEDY";
            case WORKSHOP -> "WORKSHOP";
            case OTHER -> "OTHER";

            case EVENT_CATEGORY_UNSPECIFIED ->
                    throw new IllegalArgumentException(
                            "Event category cannot be unspecified"
                    );

            case UNRECOGNIZED ->
                    throw new IllegalArgumentException(
                            "Unrecognized event category"
                    );
        };
    }

    private static String mapStatus(EventStatus status) {

        return switch (status) {
            case DRAFT -> "DRAFT";
            case PUBLISHED -> "PUBLISHED";
            case CANCELLED -> "CANCELLED";
            case SOLD_OUT -> "SOLD_OUT";

            case EVENT_STATUS_UNSPECIFIED ->
                    throw new IllegalArgumentException(
                            "Event status cannot be unspecified"
                    );

            case UNRECOGNIZED ->
                    throw new IllegalArgumentException(
                            "Unrecognized event status"
                    );
        };
    }
}
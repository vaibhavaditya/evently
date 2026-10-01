package com.evently.evtnotificationservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {

    private String eventId;      // unique Kafka message ID
    private String entityId;     // actual Event ID
    private String eventType;
    private Instant occurredAt;
    private EventPayload payload;
}
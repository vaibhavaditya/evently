package com.evently.evtopenservice.kafka;

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

    private String eventId;
    private String entityId;
    private String eventType;
    private Instant occurredAt;
    private EventPayload payload;
}
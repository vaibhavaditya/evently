package com.evently.evtnotificationservice.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event_notifications")
public class EventNotification {

    @Id
    private String id;

    private String eventId;      // unique Kafka message ID
    private String entityId;     // actual Event ID

    private String eventType;
    private Instant occurredAt;

    private String eventName;
    private String city;
    private String category;
    private String status;

    private Instant processedAt;
}
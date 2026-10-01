package com.evently.evtnotificationservice.notification;

import com.evently.evtnotificationservice.kafka.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EventNotificationRepository repository;

    public boolean processEvent(EventMessage message) {

        // eventId = unique ID of this Kafka message
        String eventId = message.getEventId();

        /*
         * Idempotency check.
         *
         * If this Kafka message was already processed,
         * do not process it again.
         */
        if (repository.findByEventId(eventId).isPresent()) {
            return false;
        }

        EventNotification notification =
                new EventNotification();

        // Unique Kafka message ID
        notification.setEventId(message.getEventId());

        // Actual Event ID
        notification.setEntityId(message.getEntityId());

        notification.setEventType(message.getEventType());
        notification.setOccurredAt(message.getOccurredAt());

        notification.setEventName(
                message.getPayload().getEventName()
        );

        notification.setCity(
                message.getPayload().getCity()
        );

        notification.setCategory(
                message.getPayload().getCategory()
        );

        notification.setStatus(
                message.getPayload().getStatus()
        );

        notification.setProcessedAt(Instant.now());

        repository.save(notification);

        return true;
    }
}
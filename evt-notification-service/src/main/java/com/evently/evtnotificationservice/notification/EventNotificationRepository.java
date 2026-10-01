package com.evently.evtnotificationservice.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventNotificationRepository
        extends MongoRepository<EventNotification, String> {

    Optional<EventNotification> findByEventId(String eventId);

    Optional<EventNotification> findTopByEntityIdOrderByProcessedAtDesc(
            String entityId
    );
}
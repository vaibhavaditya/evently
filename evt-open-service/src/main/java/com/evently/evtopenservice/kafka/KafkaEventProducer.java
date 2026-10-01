package com.evently.evtopenservice.kafka;

import com.evently.grpc.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, EventMessage> kafkaTemplate;

    public void publishEvent(
            Event event,
            EventType eventType
    ) {

        EventMessage message =
                KafkaEventMapper.toMessage(event, eventType);

        String topic = getTopic(eventType);

        kafkaTemplate.send(
                topic,
                event.getId(),
                message
        );
    }

    private String getTopic(EventType eventType) {

        return switch (eventType) {

            case EVENT_PUBLISHED ->
                    KafkaTopics.EVENT_PUBLISHED;

            case EVENT_STATUS_CHANGED ->
                    KafkaTopics.EVENT_STATUS_CHANGED;
        };
    }
}
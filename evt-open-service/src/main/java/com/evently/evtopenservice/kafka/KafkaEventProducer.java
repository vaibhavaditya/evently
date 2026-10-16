
package com.evently.evtopenservice.kafka;

import com.evently.grpc.Event;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaEventProducer.class);

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final KafkaTemplate<String, EventMessage> kafkaTemplate;

    public void publishEvent(Event event, EventType eventType) {

        EventMessage message =
                KafkaEventMapper.toMessage(event, eventType);

        String topic = getTopic(eventType);
        String traceId = MDC.get("traceId");

        ProducerRecord<String, EventMessage> record =
                new ProducerRecord<>(
                        topic,
                        event.getId(),
                        message
                );

        if (traceId != null && !traceId.isBlank()) {
            record.headers().add(
                    TRACE_ID_HEADER,
                    traceId.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            log.info(
                    "Publishing Kafka event with trace ID {} to topic {}",
                    traceId,
                    topic
            );
        } else {
            log.warn(
                    "Publishing Kafka event without trace ID to topic {}",
                    topic
            );
        }

        kafkaTemplate.send(record);
    }

    private String getTopic(EventType eventType) {
        return switch (eventType) {
            case EVENT_PUBLISHED -> KafkaTopics.EVENT_PUBLISHED;
            case EVENT_STATUS_CHANGED -> KafkaTopics.EVENT_STATUS_CHANGED;
        };
    }
}

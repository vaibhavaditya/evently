package com.evently.evtnotificationservice.kafka;

import com.evently.evtnotificationservice.dashboard.DashboardService;
import com.evently.evtnotificationservice.notification.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventKafkaListener {

    private final NotificationService notificationService;
    private final DashboardService dashboardService;

    @PostConstruct
    public void test() {
        System.out.println("🔥 EventKafkaListener bean CREATED");
    }

    @KafkaListener(
            topics = {
                    "event.published",
                    "event.status.changed"
            },
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, EventMessage> record,
            Acknowledgment acknowledgment
    ) {
        String traceId = null;

        try {
            // Extract trace ID from Kafka headers
            var header = record.headers().lastHeader("X-Trace-Id");

            if (header != null) {
                traceId = new String(
                        header.value(),
                        StandardCharsets.UTF_8
                );
                MDC.put("traceId", traceId);

                log.info("Trace ID extracted from Kafka header: {}", traceId);

            } else {
                log.warn("X-Trace-Id header is missing from Kafka message");
            }
            EventMessage message = record.value();

            log.info(
                    "Received Kafka event: eventId={}, eventType={}, traceId={}",
                    message.getEventId(),
                    message.getEventType(),
                    traceId
            );

            boolean processed =
                        notificationService.processEvent(message);

            if (processed) {
                dashboardService.updateDashboard(message);

                log.info(
                        "Event processed successfully: eventId={}",
                        message.getEventId()
                );
            } else {
                log.info(
                        "Duplicate event ignored: eventId={}",
                        message.getEventId()
                );
            }

            acknowledgment.acknowledge();

        } finally {
            MDC.remove("traceId");
        }
    }
}
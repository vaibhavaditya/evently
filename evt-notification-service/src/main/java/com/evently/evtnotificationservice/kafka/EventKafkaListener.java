package com.evently.evtnotificationservice.kafka;

import com.evently.evtnotificationservice.dashboard.DashboardService;
import com.evently.evtnotificationservice.notification.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
            EventMessage message,
            Acknowledgment acknowledgment
    ) {

        log.info(
                "Received Kafka event: eventId={}, eventType={}",
                message.getEventId(),
                message.getEventType()
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
    }
}
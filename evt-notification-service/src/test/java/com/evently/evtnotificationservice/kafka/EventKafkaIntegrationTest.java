package com.evently.evtnotificationservice.kafka;

import com.evently.evtnotificationservice.dashboard.CityDashboard;
import com.evently.evtnotificationservice.dashboard.CityDashboardRepository;
import com.evently.evtnotificationservice.notification.EventNotificationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class EventKafkaIntegrationTest {

    // KAFKA CONTAINER

    @Container
    static KafkaContainer kafka =
            new KafkaContainer("apache/kafka-native:3.8.0");

    // MONGODB CONTAINER

    @Container
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    // DYNAMIC TEST PROPERTIES

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );

        registry.add(
                "spring.mongodb.uri",
                mongo::getReplicaSetUrl
        );
    }

    // AUTOWIRED DEPENDENCIES

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private EventNotificationRepository notificationRepository;

    @Autowired
    private CityDashboardRepository dashboardRepository;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    // WAIT FOR KAFKA CONSUMER TO BECOME READY

    private void waitForKafkaConsumer() {

        await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {

                    var containers = registry.getListenerContainers();

                    assertThat(containers).isNotEmpty();

                    var container = containers.iterator().next();

                    assertThat(container)
                            .isInstanceOf(AbstractMessageListenerContainer.class);

                    var kafkaContainer =
                            (AbstractMessageListenerContainer<?, ?>) container;

                    assertThat(kafkaContainer.getAssignedPartitions())
                            .isNotNull()
                            .isNotEmpty();
                });
    }

    // CLEAN DATABASE BEFORE EACH TEST

    @BeforeEach
    void cleanDatabase() {

        waitForKafkaConsumer();

        notificationRepository.deleteAll();
        dashboardRepository.deleteAll();
    }

    // CREATE TEST EVENT MESSAGE

    private EventMessage createEventMessage(
            String eventId,
            String entityId
    ) {

        EventPayload payload = new EventPayload(
                "Tech Fest",
                "Patna",
                "WORKSHOP",
                "PUBLISHED"
        );

        return new EventMessage(
                eventId,
                entityId,
                "EVENT_PUBLISHED",
                Instant.now(),
                payload
        );
    }

    // TEST 1: CONSUME EVENT AND UPDATE MONGODB

    @Test
    void shouldConsumeEventAndUpdateNotificationAndDashboard()
            throws Exception {

        EventMessage message = createEventMessage(
                "msg-101",
                "event-501"
        );

        // Publish message and wait for Kafka broker acknowledgement
        kafkaTemplate.send(
                "event.published",
                message.getEventId(),
                message
        ).get(10, TimeUnit.SECONDS);

        // Wait for asynchronous consumer processing
        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {

                    var notification =
                            notificationRepository.findByEventId("msg-101");

                    assertThat(notification).isPresent();

                    assertThat(notification.get().getEntityId())
                            .isEqualTo("event-501");

                    assertThat(notification.get().getEventName())
                            .isEqualTo("Tech Fest");

                    var dashboard =
                            dashboardRepository.findById("Patna");

                    assertThat(dashboard).isPresent();

                    CityDashboard cityDashboard = dashboard.get();

                    assertThat(cityDashboard.getTotalEvents())
                            .isEqualTo(1L);

                    assertThat(cityDashboard.getPublishedEvents())
                            .isEqualTo(1L);

                    assertThat(cityDashboard.getEventsByCategory())
                            .containsEntry("WORKSHOP", 1L);

                    assertThat(cityDashboard.getEventStatuses())
                            .containsEntry("event-501", "PUBLISHED");
                });
    }

    // TEST 2: DUPLICATE EVENT SHOULD BE IGNORED

    @Test
    void shouldIgnoreDuplicateEventAndUpdateDashboardOnlyOnce()
            throws Exception {

        EventMessage message = createEventMessage(
                "msg-duplicate-201",
                "event-601"
        );

        // Publish the same message for the first time
        kafkaTemplate.send(
                "event.published",
                message.getEventId(),
                message
        ).get(10, TimeUnit.SECONDS);

        // Publish the exact same message again
        kafkaTemplate.send(
                "event.published",
                message.getEventId(),
                message
        ).get(10, TimeUnit.SECONDS);

        // Wait for the first message to be processed
        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {

                    var notification =
                            notificationRepository.findByEventId(
                                    "msg-duplicate-201"
                            );

                    assertThat(notification).isPresent();

                    var dashboard =
                            dashboardRepository.findById("Patna");

                    assertThat(dashboard).isPresent();

                    CityDashboard cityDashboard = dashboard.get();

                    // Event should be counted only once
                    assertThat(cityDashboard.getTotalEvents())
                            .isEqualTo(1L);

                    assertThat(cityDashboard.getPublishedEvents())
                            .isEqualTo(1L);

                    assertThat(cityDashboard.getEventsByCategory())
                            .containsEntry("WORKSHOP", 1L);

                    assertThat(cityDashboard.getEventStatuses())
                            .containsEntry("event-601", "PUBLISHED");

                    // Only one notification should exist
                    assertThat(notificationRepository.count())
                            .isEqualTo(1L);
                });

        // Allow time for the second delivery to be processed
        await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {

                    assertThat(notificationRepository.count())
                            .isEqualTo(1L);

                    CityDashboard dashboard =
                            dashboardRepository.findById("Patna")
                                    .orElseThrow();

                    assertThat(dashboard.getTotalEvents())
                            .isEqualTo(1L);

                    assertThat(dashboard.getPublishedEvents())
                            .isEqualTo(1L);
                });
    }
}
package com.evently.evtcoreservice.repository;

import com.evently.evtcoreservice.entity.Event;
import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.evently.evtcoreservice.repository.EventSpecifications; // Adjust package if needed

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
})
class EventRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("evently_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EventRepository eventRepository;

    private Event createEvent(String name, String city, EventStatus status) {

        Event event = new Event();

        event.setEventName(name);
        event.setOrganizerName("Test Organizer");
        event.setOrganizerMobile("9876543210" + name.length() + name.hashCode());
        event.setCity(city);
        event.setCategory(EventCategory.WORKSHOP);
        event.setStatus(status);

        event.setCreatedOn(LocalDateTime.now());
        event.setModifiedOn(LocalDateTime.now());

        return event;
    }

    @Test
    void testPostgresConnection() {
        assertThat(eventRepository.count()).isZero();
    }

    @Test
    void shouldFilterEventsByCityAndStatus() {

        Event e1 = createEvent("Tech Fest", "Patna", EventStatus.PUBLISHED);
        Event e2 = createEvent("Music Night", "Patna", EventStatus.PUBLISHED);
        Event e3 = createEvent("Comedy Show", "Patna", EventStatus.DRAFT);
        Event e4 = createEvent("Sports Meet", "Delhi", EventStatus.PUBLISHED);
        Event e5 = createEvent("Workshop", "Delhi", EventStatus.DRAFT);

        eventRepository.saveAll(List.of(e1, e2, e3, e4, e5));
        eventRepository.flush();

        Specification<Event> specification =
                EventSpecifications.hasCity("Patna")
                        .and(EventSpecifications.hasStatus(EventStatus.PUBLISHED));

        Page<Event> result = eventRepository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(Event::getEventName)
                .containsExactlyInAnyOrder("Tech Fest", "Music Night");
    }

    @Test
    void shouldReturnEventsWithPagination() {

        // Arrange: Insert 5 events
        eventRepository.saveAll(List.of(
                createEvent("Event 1", "Patna", EventStatus.PUBLISHED),
                createEvent("Event 2", "Patna", EventStatus.PUBLISHED),
                createEvent("Event 3", "Patna", EventStatus.PUBLISHED),
                createEvent("Event 4", "Patna", EventStatus.PUBLISHED),
                createEvent("Event 5", "Patna", EventStatus.PUBLISHED)
        ));

        eventRepository.flush();

        // Act: Fetch first page with 2 events
        Page<Event> result = eventRepository.findAll(
                PageRequest.of(0, 2)
        );

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    void shouldRejectDuplicateOrganizerMobileAtDatabaseLevel() {

        // Arrange: First event
        Event event1 = createEvent(
                "Tech Fest",
                "Patna",
                EventStatus.PUBLISHED
        );

        event1.setOrganizerMobile("9876543210");

        eventRepository.saveAndFlush(event1);

        // Arrange: Second event with the same mobile
        Event event2 = createEvent(
                "Music Night",
                "Delhi",
                EventStatus.PUBLISHED
        );

        event2.setOrganizerMobile("9876543210");

        // Act + Assert
        assertThatThrownBy(() -> {
            eventRepository.saveAndFlush(event2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
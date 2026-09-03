package com.evently.evtcoreservice.service;

import com.evently.evtcoreservice.dto.EventDTO;
import com.evently.evtcoreservice.dto.EventListResponse;
import com.evently.evtcoreservice.dto.EventRequest;
import com.evently.evtcoreservice.dto.EventResponse;
import com.evently.evtcoreservice.dto.EventStatsResponse;
import com.evently.evtcoreservice.dto.StatusUpdateRequest;
import com.evently.evtcoreservice.entity.Event;
import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import com.evently.evtcoreservice.exception.BadRequestException;
import com.evently.evtcoreservice.exception.ConflictException;
import com.evently.evtcoreservice.exception.ResourceNotFoundException;
import com.evently.evtcoreservice.mapper.EventMapper;
import com.evently.evtcoreservice.repository.EventRepository;
import com.evently.evtcoreservice.repository.specification.EventSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse createEvent(EventRequest eventRequest) {

        if (eventRepository.existsByOrganizerMobile(
                eventRequest.getOrganizerMobile())) {

            throw new ConflictException(
                    "Organizer mobile already exists");
        }

        Event event = eventMapper.toEntity(eventRequest);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public EventResponse getEventById(UUID id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found with id: " + id));

        return eventMapper.toResponse(event);
    }

    @Override
    public EventListResponse getEvents(
            String city,
            EventCategory category,
            EventStatus status,
            int page,
            int size) {

        if (page < 0) {
            throw new BadRequestException(
                    "Page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new BadRequestException(
                    "Size must be greater than 0");
        }

        Specification<Event> specification =
                Specification.unrestricted();

        if (city != null && !city.isBlank()) {
            specification = specification.and(
                    EventSpecifications.hasCity(city));
        }

        if (category != null) {
            specification = specification.and(
                    EventSpecifications.hasCategory(category));
        }

        if (status != null) {
            specification = specification.and(
                    EventSpecifications.hasStatus(status));
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Event> eventPage =
                eventRepository.findAll(specification, pageable);

        List<EventDTO> events = eventPage
                .getContent()
                .stream()
                .map(eventMapper::toDTO)
                .toList();

        return new EventListResponse(
                events,
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Override
    public EventResponse updateStatus(
            UUID id,
            StatusUpdateRequest statusUpdateRequest) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found with id: " + id));

        EventStatus currentStatus = event.getStatus();
        EventStatus newStatus = statusUpdateRequest.getStatus();

        boolean validTransition =
                currentStatus == EventStatus.DRAFT
                        && newStatus == EventStatus.PUBLISHED;

        if (currentStatus == EventStatus.PUBLISHED
                && (newStatus == EventStatus.CANCELLED
                || newStatus == EventStatus.SOLD_OUT)) {

            validTransition = true;
        }

        if (!validTransition) {
            throw new BadRequestException(
                    "Cannot change status from "
                            + currentStatus
                            + " to "
                            + newStatus);
        }

        event.setStatus(newStatus);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public EventStatsResponse getStats() {

        List<Event> events = eventRepository.findAll();

        Map<EventStatus, Long> byStatus =
                events.stream()
                        .collect(Collectors.groupingBy(
                                Event::getStatus,
                                Collectors.counting()
                        ));

        Map<EventCategory, Long> byCategory =
                events.stream()
                        .collect(Collectors.groupingBy(
                                Event::getCategory,
                                Collectors.counting()
                        ));

        return new EventStatsResponse(
                events.size(),
                byStatus,
                byCategory
        );
    }
}
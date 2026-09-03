package com.evently.evtcoreservice.service;

import com.evently.evtcoreservice.dto.EventListResponse;
import com.evently.evtcoreservice.dto.EventRequest;
import com.evently.evtcoreservice.dto.EventResponse;
import com.evently.evtcoreservice.dto.EventStatsResponse;
import com.evently.evtcoreservice.dto.StatusUpdateRequest;
import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;

import java.util.UUID;

public interface EventService {

    EventResponse createEvent(EventRequest eventRequest);

    EventResponse getEventById(UUID id);

    EventListResponse getEvents(
            String city,
            EventCategory category,
            EventStatus status,
            int page,
            int size
    );

    EventResponse updateStatus(
            UUID id,
            StatusUpdateRequest statusUpdateRequest
    );

    EventStatsResponse getStats();
}
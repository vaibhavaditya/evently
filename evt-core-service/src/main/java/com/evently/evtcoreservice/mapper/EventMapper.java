package com.evently.evtcoreservice.mapper;

import com.evently.evtcoreservice.dto.EventDTO;
import com.evently.evtcoreservice.dto.EventRequest;
import com.evently.evtcoreservice.dto.EventResponse;
import com.evently.evtcoreservice.entity.Event;
import com.evently.evtcoreservice.enums.EventStatus;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequest request) {

        Event event = new Event();

        event.setEventName(request.getEventName());
        event.setOrganizerName(request.getOrganizerName());
        event.setOrganizerMobile(request.getOrganizerMobile());
        event.setCity(request.getCity());
        event.setCategory(request.getCategory());

        // Every newly created event starts as DRAFT
        event.setStatus(EventStatus.DRAFT);

        return event;
    }

    public EventDTO toDTO(Event event) {

        return new EventDTO(
                event.getId(),
                event.getEventName(),
                event.getOrganizerName(),
                event.getOrganizerMobile(),
                event.getCity(),
                event.getCategory(),
                event.getStatus(),
                event.getCreatedOn(),
                event.getModifiedOn()
        );
    }

    public EventResponse toResponse(Event event) {

        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getOrganizerName(),
                event.getOrganizerMobile(),
                event.getCity(),
                event.getCategory(),
                event.getStatus(),
                event.getCreatedOn(),
                event.getModifiedOn()
        );
    }
}
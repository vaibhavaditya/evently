package com.evently.evtopenservice.mapper;

import com.evently.evtopenservice.dto.EventHttpResponse;
import com.evently.grpc.Event;

public class EventMapper {

    public static EventHttpResponse toHttp(Event event) {

        EventHttpResponse httpResponse =
                new EventHttpResponse();

        httpResponse.setId(event.getId());
        httpResponse.setEventName(event.getEventName());
        httpResponse.setOrganizerName(event.getOrganizerName());
        httpResponse.setOrganizerMobile(event.getOrganizerMobile());
        httpResponse.setCity(event.getCity());

        httpResponse.setCategory(
                event.getCategory().name()
        );

        httpResponse.setStatus(
                event.getStatus().name()
        );

        return httpResponse;
    }
}
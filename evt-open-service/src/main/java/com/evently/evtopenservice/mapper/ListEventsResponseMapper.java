package com.evently.evtopenservice.mapper;

import com.evently.evtopenservice.dto.EventHttpResponse;
import com.evently.evtopenservice.dto.ListEventsHttpResponse;
import com.evently.grpc.ListEventsResponse;

import java.util.List;
public class ListEventsResponseMapper {
    public static ListEventsHttpResponse toHttp(ListEventsResponse response){

        List<EventHttpResponse> events = response.getEventsList()
                                            .stream()
                                            .map(EventMapper::toHttp)
                                            .toList();

        ListEventsHttpResponse httpResponse = new ListEventsHttpResponse();
        httpResponse.setEvents(events);
        httpResponse.setTotalElements(response.getTotalElements());
        httpResponse.setTotalPages(response.getTotalPages());

        return httpResponse;
    }
}

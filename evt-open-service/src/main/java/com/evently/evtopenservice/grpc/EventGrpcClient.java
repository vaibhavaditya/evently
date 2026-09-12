package com.evently.evtopenservice.grpc;

import com.evently.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventGrpcClient {

    private final EventServiceGrpc.EventServiceBlockingStub eventServiceStub;

    public CreateEventResponse createEvent(CreateEventRequest request){
        return eventServiceStub.createEvent(request);
    }

    public GetEventResponse getEvent(GetEventRequest request) {
        return eventServiceStub.getEvent(request);
    }

    public ListEventsResponse listEvents(ListEventsRequest request) {
        return eventServiceStub.listEvents(request);
    }

    public UpdateEventStatusResponse updateEventStatus(UpdateEventStatusRequest request) {
        return eventServiceStub.updateEventStatus(request);
    }

    public GetEventStatsResponse getEventStats(GetEventStatsRequest request) {

        return eventServiceStub.getEventStats(request);
    }
}

package com.evently.evtopenservice.mapper;

import com.evently.evtopenservice.dto.CreateEventHttpRequest;
import com.evently.grpc.CreateEventRequest;
import com.evently.grpc.EventCategory;

public class CreateEventMapper {
    public static CreateEventRequest toGrpc(CreateEventHttpRequest request){
        return CreateEventRequest.newBuilder()
                .setEventName(request.getEventName())
                .setOrganizerName(request.getOrganizerName())
                .setOrganizerMobile(request.getOrganizerMobile())
                .setCity(request.getCity())
                .setCategory(
                        EventCategory.valueOf(request.getCategory())
                )
                .build();
    }
}

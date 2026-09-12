package com.evently.evtopenservice.mapper;

import com.evently.evtopenservice.dto.UpdateEventStatusHttpRequest;
import com.evently.grpc.EventStatus;
import com.evently.grpc.UpdateEventStatusRequest;

public class UpdateEventStatusMapper {

    public static UpdateEventStatusRequest toGrpc(
            String id,
            UpdateEventStatusHttpRequest request) {

        return UpdateEventStatusRequest.newBuilder()
                .setId(id)
                .setStatus(EventStatus.valueOf(request.getStatus().toUpperCase()))
                .build();
    }
}
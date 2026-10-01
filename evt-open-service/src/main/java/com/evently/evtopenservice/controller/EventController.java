package com.evently.evtopenservice.controller;

import com.evently.evtopenservice.dto.*;
import com.evently.evtopenservice.grpc.EventGrpcClient;
import com.evently.evtopenservice.kafka.EventType;
import com.evently.evtopenservice.kafka.KafkaEventProducer;
import com.evently.evtopenservice.mapper.CreateEventMapper;
import com.evently.evtopenservice.mapper.EventMapper;
import com.evently.evtopenservice.mapper.EventStatsResponseMapper;
import com.evently.evtopenservice.mapper.ListEventsResponseMapper;
import com.evently.grpc.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventGrpcClient eventGrpcClient;
    private final KafkaEventProducer kafkaEventProducer;

    @PostMapping
    public ResponseEntity<EventHttpResponse> createEvent(
            @Valid @RequestBody CreateEventHttpRequest request) {

        CreateEventRequest grpcRequest =
                CreateEventMapper.toGrpc(request);

        CreateEventResponse grpcResponse =
                eventGrpcClient.createEvent(grpcRequest);

        kafkaEventProducer.publishEvent(
                grpcResponse.getEvent(),
                EventType.EVENT_PUBLISHED
        );

        EventHttpResponse httpResponse =
                EventMapper.toHttp(grpcResponse.getEvent());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(httpResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventHttpResponse> getEvent(@PathVariable String id){
        GetEventRequest grpcRequest =
                GetEventRequest.newBuilder()
                        .setId(id)
                        .build();

        GetEventResponse grpcResponse =
                eventGrpcClient.getEvent(grpcRequest);

        EventHttpResponse httpResponse =
                EventMapper.toHttp((grpcResponse.getEvent()));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(httpResponse);
    }

    @GetMapping
    public ResponseEntity<ListEventsHttpResponse> getEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        ListEventsRequest.Builder builder =
                ListEventsRequest.newBuilder()
                        .setPage(page)
                        .setSize(size);

        if (city != null && !city.isBlank()) {
            builder.setCity(city);
        }

        if (category != null && !category.isBlank()) {
            builder.setCategory(
                    EventCategory.valueOf(category.toUpperCase())
            );
        }

        if (status != null && !status.isBlank()) {
            builder.setStatus(
                    EventStatus.valueOf(status.toUpperCase())
            );
        }

        ListEventsResponse grpcResponse =
                eventGrpcClient.listEvents(builder.build());

        ListEventsHttpResponse httpResponse =
                ListEventsResponseMapper.toHttp(grpcResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(httpResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventHttpResponse> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateEventStatusHttpRequest request){

        UpdateEventStatusRequest grpcRequest =
                UpdateEventStatusRequest.newBuilder()
                        .setId(id)
                        .setStatus(EventStatus.valueOf(request.getStatus().toUpperCase()))
                        .build();

        UpdateEventStatusResponse grpcResponse =
                eventGrpcClient.updateEventStatus(grpcRequest);

        kafkaEventProducer.publishEvent(
                grpcResponse.getEvent(),
                EventType.EVENT_STATUS_CHANGED
        );

        EventHttpResponse httpResponse =
                EventMapper.toHttp(grpcResponse.getEvent());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(httpResponse);
    }

    @GetMapping("/stats")
    public ResponseEntity<EventStatsHttpResponse> getStats() {
        GetEventStatsRequest grpcRequest =
                GetEventStatsRequest.newBuilder()
                        .build();

        GetEventStatsResponse grpcResponse =
                eventGrpcClient.getEventStats(grpcRequest);

        EventStatsHttpResponse httpResponse =
                EventStatsResponseMapper.toHttp(grpcResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(httpResponse);

    }
}

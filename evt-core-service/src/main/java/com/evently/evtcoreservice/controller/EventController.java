package com.evently.evtcoreservice.controller;

import com.evently.evtcoreservice.dto.ApiResponse;
import com.evently.evtcoreservice.dto.EventListResponse;
import com.evently.evtcoreservice.dto.EventRequest;
import com.evently.evtcoreservice.dto.EventResponse;
import com.evently.evtcoreservice.dto.EventStatsResponse;
import com.evently.evtcoreservice.dto.StatusUpdateRequest;
import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import com.evently.evtcoreservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest eventRequest) {

        EventResponse eventResponse =
                eventService.createEvent(eventRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(eventResponse));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<EventStatsResponse>> getStats() {

        EventStatsResponse eventStatsResponse =
                eventService.getStats();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(eventStatsResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable UUID id) {

        EventResponse eventResponse =
                eventService.getEventById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(eventResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EventListResponse>> getEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        EventListResponse eventListResponse =
                eventService.getEvents(
                        city,
                        category,
                        status,
                        page,
                        size
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(eventListResponse));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EventResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest statusUpdateRequest) {

        EventResponse eventResponse =
                eventService.updateStatus(
                        id,
                        statusUpdateRequest
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(eventResponse));
    }
}
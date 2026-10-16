
package com.evently.evtbff.controller;

import com.evently.evtbff.client.EventOpenClient;
import com.evently.evtbff.dto.*;
import com.evently.evtbff.security.RoleRequired;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventOpenClient eventOpenClient;

    @RoleRequired("ADMIN")
    @PostMapping
    public ResponseEntity<EventHttpResponse> createEvent(
            @Valid @RequestBody CreateEventHttpRequest request
    ) {
        EventHttpResponse response =
                eventOpenClient.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventHttpResponse> getEvent(
            @PathVariable String id
    ) {
        EventHttpResponse response =
                eventOpenClient.getEvent(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ListEventsHttpResponse> listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ListEventsHttpResponse response =
                eventOpenClient.listEvents(
                        city,
                        category,
                        status,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @RoleRequired("ADMIN")
    @PatchMapping("/{id}/status")
    public ResponseEntity<EventHttpResponse> updateEventStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateEventStatusHttpRequest request
    ) {
        EventHttpResponse response =
                eventOpenClient.updateEventStatus(id, request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/stats")
    public ResponseEntity<EventStatsHttpResponse> getEventStats() {
        EventStatsHttpResponse response =
                eventOpenClient.getEventStats();

        return ResponseEntity.ok(response);
    }
}

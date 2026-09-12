package com.evently.evtbff.client;

import com.evently.evtbff.config.FeignConfig;
import com.evently.evtbff.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "event-open-service",
        url = "${event.open-service.url}",
        configuration = FeignConfig.class
)
public interface EventOpenClient {

    @PostMapping("/v1/events")
    EventHttpResponse createEvent(
            @RequestBody CreateEventHttpRequest request
    );

    @GetMapping("/v1/events/{id}")
    EventHttpResponse getEvent(
            @PathVariable("id") String id
    );

    @GetMapping("/v1/events")
    ListEventsHttpResponse listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @PatchMapping("/v1/events/{id}/status")
    EventHttpResponse updateEventStatus(
            @PathVariable("id") String id,
            @RequestBody UpdateEventStatusHttpRequest request
    );

    @GetMapping("/v1/events/stats")
    EventStatsHttpResponse getEventStats();
}
package com.evently.evtnotificationservice.dashboard;

import com.evently.evtnotificationservice.kafka.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CityDashboardRepository repository;

    public void updateDashboard(EventMessage message) {

        String city = message.getPayload().getCity();

        CityDashboard dashboard =
                repository.findById(city)
                        .orElseGet(() -> createDashboard(city));

        if (dashboard.getEventsByCategory() == null) {
            dashboard.setEventsByCategory(new HashMap<>());
        }

        if (dashboard.getEventStatuses() == null) {
            dashboard.setEventStatuses(new HashMap<>());
        }

        String entityId = message.getEntityId();

        String newStatus =
                message.getPayload().getStatus();

        /*
         * EVENT_PUBLISHED means this is a new event.
         */
        if ("EVENT_PUBLISHED".equals(message.getEventType())) {

            dashboard.setTotalEvents(
                    dashboard.getTotalEvents() + 1
            );

            String category =
                    message.getPayload().getCategory();

            dashboard.getEventsByCategory().merge(
                    category,
                    1L,
                    Long::sum
            );

            dashboard.getEventStatuses().put(
                    entityId,
                    newStatus
            );

            incrementStatus(dashboard, newStatus);
        }

        /*
         * EVENT_STATUS_CHANGED means an existing
         * event changed its status.
         */
        else if ("EVENT_STATUS_CHANGED".equals(message.getEventType())) {

            String oldStatus =
                    dashboard.getEventStatuses().get(entityId);

            // Remove the old status count
            if (oldStatus != null) {
                decrementStatus(dashboard, oldStatus);
            }

            // Add the new status count
            incrementStatus(dashboard, newStatus);

            // Remember the new status
            dashboard.getEventStatuses().put(
                    entityId,
                    newStatus
            );
        }

        repository.save(dashboard);
    }

    private void incrementStatus(
            CityDashboard dashboard,
            String status
    ) {

        switch (status) {

            case "PUBLISHED" ->
                    dashboard.setPublishedEvents(
                            dashboard.getPublishedEvents() + 1
                    );

            case "CANCELLED" ->
                    dashboard.setCancelledEvents(
                            dashboard.getCancelledEvents() + 1
                    );

            case "SOLD_OUT" ->
                    dashboard.setSoldOutEvents(
                            dashboard.getSoldOutEvents() + 1
                    );

            case "DRAFT" -> {
                // Nothing to increment.
            }

            default ->
                    throw new IllegalArgumentException(
                            "Unknown event status: " + status
                    );
        }
    }

    private void decrementStatus(
            CityDashboard dashboard,
            String status
    ) {

        switch (status) {

            case "PUBLISHED" ->
                    dashboard.setPublishedEvents(
                            Math.max(
                                    0,
                                    dashboard.getPublishedEvents() - 1
                            )
                    );

            case "CANCELLED" ->
                    dashboard.setCancelledEvents(
                            Math.max(
                                    0,
                                    dashboard.getCancelledEvents() - 1
                            )
                    );

            case "SOLD_OUT" ->
                    dashboard.setSoldOutEvents(
                            Math.max(
                                    0,
                                    dashboard.getSoldOutEvents() - 1
                            )
                    );

            case "DRAFT" -> {
                // Nothing to decrement.
            }

            default ->
                    throw new IllegalArgumentException(
                            "Unknown event status: " + status
                    );
        }
    }

    private CityDashboard createDashboard(String city) {

        CityDashboard dashboard =
                new CityDashboard();

        dashboard.setCity(city);
        dashboard.setTotalEvents(0);
        dashboard.setPublishedEvents(0);
        dashboard.setCancelledEvents(0);
        dashboard.setSoldOutEvents(0);
        dashboard.setEventsByCategory(new HashMap<>());
        dashboard.setEventStatuses(new HashMap<>());

        return dashboard;
    }

    public CityDashboard getDashboard(String city) {

        return repository.findById(city)
                .orElseGet(() -> createDashboard(city));
    }
}
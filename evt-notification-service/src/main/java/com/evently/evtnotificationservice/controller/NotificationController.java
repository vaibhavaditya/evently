package com.evently.evtnotificationservice.controller;

import com.evently.evtnotificationservice.dashboard.CityDashboard;
import com.evently.evtnotificationservice.dashboard.DashboardService;
import com.evently.evtnotificationservice.notification.EventNotification;
import com.evently.evtnotificationservice.notification.EventNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final EventNotificationRepository notificationRepository;
    private final DashboardService dashboardService;

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotification(
            @RequestParam String entityId
    ) {

        Optional<EventNotification> notification =
                notificationRepository
                        .findTopByEntityIdOrderByProcessedAtDesc(entityId);

        if (notification.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notification.get());
    }

    @GetMapping("/dashboard/{city}")
    public ResponseEntity<CityDashboard> getDashboard(
            @PathVariable String city
    ) {

        CityDashboard dashboard =
                dashboardService.getDashboard(city);

        return ResponseEntity.ok(dashboard);
    }
}
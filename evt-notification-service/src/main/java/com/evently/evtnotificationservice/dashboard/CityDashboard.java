package com.evently.evtnotificationservice.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "city_dashboards")
public class CityDashboard {

    @Id
    private String city;

    private long totalEvents;
    private long publishedEvents;
    private long cancelledEvents;
    private long soldOutEvents;

    private Map<String, Long> eventsByCategory = new HashMap<>();

    // eventId -> current status
    private Map<String, String> eventStatuses = new HashMap<>();
}
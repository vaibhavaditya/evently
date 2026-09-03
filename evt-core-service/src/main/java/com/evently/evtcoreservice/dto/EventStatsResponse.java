package com.evently.evtcoreservice.dto;

import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class EventStatsResponse {

    private long totalEvents;
    private Map<EventStatus, Long> byStatus;
    private Map<EventCategory, Long> byCategory;
}
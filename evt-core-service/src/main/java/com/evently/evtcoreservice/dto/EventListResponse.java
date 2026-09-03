package com.evently.evtcoreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EventListResponse {

    private List<EventDTO> events;
    private long totalElements;
    private int totalPages;
}
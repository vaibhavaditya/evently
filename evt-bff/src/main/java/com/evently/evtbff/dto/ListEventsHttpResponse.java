package com.evently.evtbff.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ListEventsHttpResponse {

    private List<EventHttpResponse> events;
    private long totalElements;
    private int totalPages;
}
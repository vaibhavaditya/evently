package com.evently.evtopenservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EventStatsHttpResponse {

    private long totalEvents;

    private List<StatusCountHttpResponse> byStatus;

    private List<CategoryCountHttpResponse> byCategory;
}
package com.evently.evtcoreservice.dto;

import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EventResponse {

    private UUID id;
    private String eventName;
    private String organizerName;
    private String organizerMobile;
    private String city;
    private EventCategory category;
    private EventStatus status;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
}
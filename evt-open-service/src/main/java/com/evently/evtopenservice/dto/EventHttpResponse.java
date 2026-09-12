package com.evently.evtopenservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventHttpResponse {

    private String id;
    private String eventName;
    private String organizerName;
    private String organizerMobile;
    private String city;
    private String category;
    private String status;
}
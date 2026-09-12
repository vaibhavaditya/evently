package com.evently.evtbff.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateEventHttpRequest {
    private String eventName;
    private String organizerName;
    private String organizerMobile;
    private String city;
    private String category;
}
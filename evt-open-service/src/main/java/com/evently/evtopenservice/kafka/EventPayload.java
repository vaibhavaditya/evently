package com.evently.evtopenservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload {

    private String eventName;
    private String city;
    private String category;
    private String status;
}
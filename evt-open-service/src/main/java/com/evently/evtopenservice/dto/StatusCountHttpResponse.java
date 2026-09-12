package com.evently.evtopenservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusCountHttpResponse {

    private String status;
    private long count;
}
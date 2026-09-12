package com.evently.evtbff.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryCountHttpResponse {

    private String category;
    private long count;
}
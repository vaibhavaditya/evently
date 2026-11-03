package com.evently.evtbff.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateEventHttpRequest {

    @NotBlank(message = "Event name is required")
    private String eventName;

    @NotBlank(message = "Organizer name is required")
    private String organizerName;

    @NotBlank(message = "Organizer mobile is required")
    private String organizerMobile;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Category is required")
    private String category;
}